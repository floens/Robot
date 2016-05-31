#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <math.h>

#include <pthread.h>
#include <wiringPi.h>
#include <wiringPiI2C.h>
#include <linux/i2c-dev.h>
#include <string.h>

#include "motor.h"

#include "i2c_helper.h"
#include "timing.h"
#include "distance.h"

#define MOTOR_PIN 0x32

static int motor_i2c_fd;

static volatile int motor_loop_run = 1;
static pthread_t motor_loop_thread;
static pthread_mutex_t motor_command_lock;
static int motor_command_pending = 0;
static uint8_t motor_command_queue[7];

static void *motor_command_loop(void *unused);

void motor_setup() {
    motor_i2c_fd = wiringPiI2CSetup(MOTOR_PIN);

    uint8_t total_power[] = {4, 250};
    if (write(motor_i2c_fd, total_power, 2) < 0) {
        perror("write motor setup");
    }

    uint8_t soft_start[] = {0x91, 23, 0};
    if (write(motor_i2c_fd, soft_start, 3) < 0) {
        perror("write motor setup 2");
    }

    pullUpDnControl(0, PUD_DOWN);

    wiringPiISR(0, INT_EDGE_RISING, motor_interrupt);

    pthread_mutex_init(&motor_command_lock, NULL);

    pthread_create(&motor_loop_thread, NULL, motor_command_loop, NULL);

    motor_stop();
}

void motor_cleanup() {
    motor_stop();

    pthread_mutex_destroy(&motor_command_lock);
    motor_loop_run = 0;
}

void motor_stop() {
    struct motor_command_packet command;
    command.speed = 0.0f;
    command.direction = 0.0f;
    motor_set(&command);
}

static volatile int interrupt_counter;

void motor_interrupt() {
//    printf("Motor interrupt %d\n", interrupt_counter);
    interrupt_counter++;
}

static float last_left = 0.0f;
static float last_right = 0.0f;

#define DIFF_REQUIRED 0.1f

void motor_set(struct motor_command_packet *motor_command) {
    uint8_t command[7];
    command[0] = 7; // Command

//    printf("motor_set speed = %f direction = %f\n", motor_command->speed, motor_command->direction);

    /*
     * 0 = command = 7
     * 1 = p1 speed high byte
     * 2 = p1 speed low byte
     * 3 = p1 direction (0 = stop, 1 = backwards, 2 = forwards)
     * 4 = p2 speed high byte
     * 5 = p2 speed low byte
     * 6 = p2 direction (0 = stop, 1 = backwards, 2 = forwards)
     */

    int send_command = 0;
    int enable_servo = 0;
    if (motor_command->speed == 0.0f && (last_left != 0.0f && last_right != 0.0f)) {
        last_left = 0;
        last_right = 0;
        command[1] = 0;
        command[2] = 0;
        command[3] = 0;
        command[4] = 0;
        command[5] = 0;
        command[6] = 0;
        send_command = 1;
        enable_servo = 1;
    } else if (fabsf(motor_command->speed) <= 1.0f && fabsf(motor_command->direction) <= 1.0f) {
        float left = motor_command->speed;
        float right = motor_command->speed;

        float direction = motor_command->direction;
        if (direction > 0.0f) {
            direction = fmaxf(0.9f, fminf(1.0f, 0.9f + direction / 10.0f));
        } else if (direction < 0.0f) {
            direction = fmaxf(-1.0f, fminf(-0.9f, -0.9f + direction / 10.0f));
        }

        float directionAbs = fabsf(direction);

        if (direction > 0.0f) {
            right *= fmaxf(0.0f, (1.0f - directionAbs));
        } else {
            left *= fmaxf(0.0f, (1.0f - directionAbs));
        }

//        printf("direction = %f left = %f right = %f\n", direction, left, right);


        float left_diff = fabsf(last_left - left);
        float right_diff = fabsf(last_right - right);

//        printf("left_diff = %f right_diff = %f\n", left_diff, right_diff);

        if (left_diff >= DIFF_REQUIRED || right_diff >= DIFF_REQUIRED) {
            last_left = left;
            last_right = right;

            int leftSpeedAbs = (int) (600.0f + fabsf(left) * (1024.0f - 600.0f));
            int rightSpeedAbs = (int) (600.0f + fabsf(right) * (1024.0f - 600.0f));

//            printf("leftSpeed = %d rightSpeed = %d\n", leftSpeedAbs, rightSpeedAbs);

            command[1] = (uint8_t) ((leftSpeedAbs >> 8) & 0xff);
            command[2] = (uint8_t) (leftSpeedAbs & 0xff);

            command[4] = (uint8_t) ((rightSpeedAbs >> 8) & 0xff);
            command[5] = (uint8_t) (rightSpeedAbs & 0xff);

            if (left > 0.0f) {
                command[3] = 2;
            } else if (left < 0.0f) {
                command[3] = 1;
            } else {
                command[3] = 0;
            }

            if (right > 0.0f) {
                command[6] = 2;
            } else if (right < 0.0f) {
                command[6] = 1;
            } else {
                command[6] = 0;
            }
            send_command = 1;
        }
    } else {
        printf("Invalid motor command\n");
    }

    if (send_command) {
        pthread_mutex_lock(&motor_command_lock);
        memcpy(motor_command_queue, command, 7);
        motor_command_pending = 1;
        pthread_mutex_unlock(&motor_command_lock);

        if (enable_servo) {
            distance_enable_servo();
        } else {
            distance_disable_servo();
        }
    }
}

static void *motor_command_loop(void *unused) {
    while (motor_loop_run) {
        int send = 0;
        uint8_t command[7];

        pthread_mutex_lock(&motor_command_lock);
        if (motor_command_pending) {
            motor_command_pending = 0;
            memcpy(command, motor_command_queue, 7);
            send = 1;
        }
        pthread_mutex_unlock(&motor_command_lock);

        if (send) {
            set_slave(motor_i2c_fd, MOTOR_PIN);

            if (write(motor_i2c_fd, command, 7) != 7) {
                perror("motor write");
            }

            sleep_ms(20 * 1000);
        } else {
            sleep_ms(2 * 1000);
        }
    }

    return NULL;
}
