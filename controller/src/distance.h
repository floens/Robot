#ifndef DISTANCE_H_
#define DISTANCE_H_

struct distance_result {
    uint16_t distance;
    uint16_t autotune;
};

void distance_setup();

void distance_enable_servo();

void distance_disable_servo();

void distance_cleanup();

void distance_set_direction(int direction);

int distance_get_direction();

struct distance_result distance_get();

#endif
