#ifndef CONTROLLER_H_
#define CONTROLLER_H_

#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <bluetooth/rfcomm.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>

#define PORT 24842
#define MAX_CLIENTS 32

#include "network.h"

int main(int argc, char *argv[]);

void controller_toggle_autoscanning();

void client_connected(struct network_connection *connection);

void client_disconnected(struct network_connection *connection);

void camera_enable();

void camera_disable();

void setup_gpio();

void setup_bluetooth();

int print_device_addr();

sdp_session_t *register_service();

void unregister_service();

int setup();

#endif
