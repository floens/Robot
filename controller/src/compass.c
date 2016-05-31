#include "timing.h"

#include <unistd.h>
#include <stdint.h>
#include <wiringPi.h>
#include <wiringPiI2C.h>
#include <linux/i2c-dev.h>
#include <stdio.h>

#include <math.h>

#include "i2c_helper.h"

#include "network.h"

#include "compass.h"

#define COMPASS_PIN 0x1e

#define M_PI 3.14159265358979323846

static int compass_i2c_fd;

void compass_setup() {
    compass_i2c_fd = wiringPiI2CSetup(COMPASS_PIN);

    // 01110000, 0 = default to 0 | 11 = 8 samples | 100 = 15 hz outputrate | 00 = normal measurement
    uint8_t register_a[] = {0x00, 0x70};
    // 10100000, 101 = set gain | 00000 = default to 0
    uint8_t register_b[] = {0x01, 0xa0};

    if (write(compass_i2c_fd, register_a, 2) != 2) {
        perror("compass register a");
    }

    if (write(compass_i2c_fd, register_b, 2) != 2) {
        perror("compass register b");
    }
}

struct compass_packet compass_get() {
    set_slave(compass_i2c_fd, COMPASS_PIN);

    uint8_t mode[] = {0x02, 0x01};
    if (write(compass_i2c_fd, mode, 2) != 2) {
        perror("compass set mode");
    }

    sleep_ms(10 * 1000);

    uint8_t read_data[] = {0x03};
    if (write(compass_i2c_fd, read_data, 1) != 1) {
        perror("compass set read");
    }

    uint8_t input[6];
    if (read(compass_i2c_fd, input, 6) != 6) {
        perror("compass read");
    }

    // printf("compass %d %d %d %d %d %d\n", input[0], input[1], input[2], input[3], input[4], input[5]);

    double x = (double) ((short) ((input[0] << 8) | input[1]));
    double y = (double) ((short) ((input[4] << 8) | input[5]));
    double z = (double) ((short) ((input[2] << 8) | input[3]));

//    printf("compass x=%f y=%f z=%f\n", x, y, z);

    double angle = atan2(y, x) * 180.0 / M_PI;
    double mag = sqrt(x * x + y * y + z * z);

    if (angle < 0) {
        angle += 180.0;
    }

//    printf("angle=%0.1f, mag=%0.1f\n", angle, mag);

    struct compass_packet compass = {
            .packet.id = PACKET_ID_COMPASS,
            .x = (float) x,
            .y = (float) y,
            .z = (float) z,
            .angle = (float) angle,
            .magnitude = (float) mag
    };

    return compass;
}
