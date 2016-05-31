#ifndef MOTOR_H_
#define MOTOR_H_

#include "protocol.h"

void motor_setup();

void motor_cleanup();

void motor_stop();

void motor_interrupt();

void motor_set(struct motor_command_packet* motor_command);

#endif
