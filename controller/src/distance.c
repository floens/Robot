#include "timing.h"

#include <stdint.h>
#include <stdio.h>
#include <unistd.h>

#include <pthread.h>
#include <wiringPi.h>
#include <wiringPiI2C.h>
#include <linux/i2c-dev.h>

#include "i2c_helper.h"

#include "distance.h"

#define SERVO_PIN 0x01
#define DISTANCE_PIN 0x70

static int distance_i2c_fd;

static pthread_t distance_loop_thread;
static volatile int distance_loop_run = 1;

static pthread_mutex_t distance_result_lock;
static struct distance_result locked_result;

static void *distance_measure_loop(void *unused);

static struct distance_result distance_measure();

void distance_setup() {
    distance_i2c_fd = wiringPiI2CSetup(DISTANCE_PIN);

    distance_enable_servo();

    distance_set_direction(50);

    pthread_mutex_init(&distance_result_lock, NULL);

    pthread_create(&distance_loop_thread, NULL, distance_measure_loop, NULL);
}

static int current_direction = 0;

void distance_cleanup() {
    distance_disable_servo();

    pthread_mutex_destroy(&distance_result_lock);

    distance_loop_run = 0;
}

static int distance_servo_enabled = 0;

void distance_enable_servo() {
    if (!distance_servo_enabled) {
        distance_servo_enabled = 1;
#ifdef ENABLE_PWM
        pinMode(SERVO_PIN, PWM_OUTPUT);
        pwmSetMode(PWM_MODE_MS);
        pwmSetClock(400);
        pwmSetRange(960); // period(hz) = 19.2Mhz / 400 / 960 = 50hz
        pwmWrite(SERVO_PIN, 0);
#endif
    }
}

void distance_disable_servo() {
    if (distance_servo_enabled) {
        distance_servo_enabled = 0;

#ifdef ENABLE_PWM
        pinMode(SERVO_PIN, OUTPUT);
#endif
    }
}

/**
 * Set the direction of the servo for the distance sensor
 * @param direction the direction of the servo 0 to 100. 0 is is right, 100 is left.
 */
void distance_set_direction(int direction) {
    if (distance_servo_enabled && direction != current_direction && direction >= 0 && direction <= 100) {
        current_direction = direction;

#ifdef ENABLE_PWM
        direction += 33;
//        printf("pwmWrite direction = %d\n", direction);

        pwmWrite(SERVO_PIN, direction);
#endif
    }
}

int distance_get_direction() {
    return current_direction;
}

struct distance_result distance_get() {
    struct distance_result res;
    pthread_mutex_lock(&distance_result_lock);
    res.distance = locked_result.distance;
    res.autotune = locked_result.autotune;
    pthread_mutex_unlock(&distance_result_lock);
    return res;
}

static void *distance_measure_loop(void *unused) {
    while (distance_loop_run) {
        struct distance_result res = distance_measure();

        pthread_mutex_lock(&distance_result_lock);
        locked_result.distance = res.distance;
        locked_result.autotune = res.autotune;
        pthread_mutex_unlock(&distance_result_lock);
    }

    return NULL;
}

static struct distance_result distance_measure() {
    set_slave(distance_i2c_fd, DISTANCE_PIN);

    // start a new measurement in centimeters
    // 0x51 Real Ranging Mode  Result in centimeters
    uint8_t mode_set[] = {0, 0x51};
    if (write(distance_i2c_fd, mode_set, 2) < 0) {
        perror("distance mode set");
    }

    // Sleep for measurement
    // Can take up to 65mS, according to the datasheet
    sleep_ms(70 * 1000);

    uint8_t read_range[] = {2};
    if (write(distance_i2c_fd, read_range, 1) < 0) {
        perror("distance read request");
    }

    // Read the distance
    uint8_t distance_array[4] = {0};
    int bytes = read(distance_i2c_fd, distance_array, 4);
    if (bytes < 0) {
        perror("distance read");
    }

    struct distance_result res;
    res.distance = (distance_array[0] << 8) + distance_array[1];
    res.autotune = (distance_array[2] << 8) + distance_array[3];
    return res;
}
