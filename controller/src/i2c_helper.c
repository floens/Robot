#include <stdio.h>
#include <linux/i2c-dev.h>

#include "i2c_helper.h"

void set_slave(int fd, int pin) {
    if (ioctl(fd, I2C_SLAVE, pin) < 0) {
        printf("could not set i2c slave!\n");
    }
}
