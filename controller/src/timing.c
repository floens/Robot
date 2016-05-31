#include <stdlib.h>
#include <stdio.h>

#define __USE_POSIX199309

#include <time.h>
#include <pthread.h>

#include "timing.h"

void sleep_ms(long long msec) {
    long long usec = msec * 1000L;

    struct timespec time;
    time.tv_sec = usec / 1000000000L;
    time.tv_nsec = usec % 1000000000L;
    if (nanosleep(&time, NULL) < 0) {
        perror("nanosleep");
    }
}

long long get_time() {
    struct timespec time;

    clock_gettime(CLOCK_MONOTONIC, &time);

    return time.tv_sec * 1000000L + time.tv_nsec / 1000L;
}
