#ifndef SLEEP_H_
#define SLEEP_H_

#define TIMING_START long long timing_start_time = get_time();
#define TIMING_END(n) printf("* " n " took %lli microseconds\n", get_time() - timing_start_time);

void sleep_ms(long long msec);

long long get_time();

#endif
