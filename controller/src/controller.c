#include "timing.h"

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <sys/resource.h>

#include <linux/i2c-dev.h>
#include <wiringPi.h>
#include <event2/event.h>

#include "controller.h"
#include "distance.h"
#include "compass.h"
#include "database.h"

#include "motor.h"
#include "openalpr.hpp"

static void sigint_handler(int signal);

static void cleanup();

void tick100(evutil_socket_t fd, short events, void *arg);

static int client_count = 0;
static struct network_connection **clients; // Array of pointers to network_connection
static int tick_counter = 0;

static int autoscanning_enabled = 0;

int main(int argc, char *argv[]) {
    printf("Initializing\n");

    // Set as a realtime process
//    if (setpriority(PRIO_PROCESS, 0, -5) < 0) {
//        perror("setpriority");
//    }

    // Set stdout to not buffer
    setvbuf(stdout, NULL, _IONBF, 0);

    // Catch sigint
    signal(SIGINT, sigint_handler);

    struct event_base *base = event_base_new();
    if (!base) {
        perror("event_base_new");
        return 1;
    }

    struct event *tick_event = event_new(base, -1, EV_PERSIST, tick100, NULL);
    struct timeval tick_time;
    tick_time.tv_sec = 0;
    tick_time.tv_usec = 100000;
    event_add(tick_event, &tick_time);

    clients = calloc(MAX_CLIENTS, sizeof(struct network_connection *));

    alpr_setup();

    setup_gpio();

    motor_setup();

    distance_setup();

    compass_setup();

    network_listen(base);

    cleanup();

    return 0;
}

static void sigint_handler(int signal) {
    cleanup();
    exit(0);
}

static void cleanup() {
    printf("Stopping\n");
    motor_cleanup();
    distance_cleanup();
    camera_disable();
}

void controller_toggle_autoscanning() {
    autoscanning_enabled = !autoscanning_enabled;
}

// Called every 100ms
void tick100(evutil_socket_t fd, short events, void *arg) {
    if (client_count > 0) {
        struct compass_packet compass_data;
        int send_compass_data = 0;
        if (tick_counter % 3 == 0) {
            // Send compass data every 300ms
            compass_data = compass_get();
            send_compass_data = 1;
        }

        struct distance_packet distance_data;
        int send_distance_data = 0;
        if (tick_counter % 5 == 0) {
            // Send distance data every 500ms
            struct distance_result distance_measured = distance_get();

            float position = (100 - distance_get_direction()) / 100.0f;

            struct distance_packet packet = {
                    .packet.id = PACKET_ID_DISTANCE,
                    .distance = distance_measured.distance,
                    .autotune = distance_measured.autotune,
                    .position = position
            };
            distance_data = packet;

            send_distance_data = 1;

            // Move position for the next measurement
            if (autoscanning_enabled) {
                int servo_position = (tick_counter * 2) % 100;
                distance_set_direction(servo_position);
            }
        }

        // Write packets
        for (int i = 0; i < client_count; i++) {
            struct network_connection *client = clients[i];

            if (send_compass_data) {
                protocol_write(client, (struct packet *) &compass_data);
            }

            if (send_distance_data) {
                protocol_write(client, (struct packet *) &distance_data);
            }

            network_flush(client);
        }

        tick_counter++;
    }
}

void client_connected(struct network_connection *connection) {
    if (client_count == MAX_CLIENTS) {
        printf("Max client count reached, ignoring");
        return;
    }

    clients[client_count] = connection;
    client_count++;

    if (client_count == 1) {
        camera_enable();
    }
}

void client_disconnected(struct network_connection *connection) {
    // Remove from array and move the rest to the left
    for (int i = 0; i < client_count; i++) {
        if (clients[i] == connection) {
            for (int j = i; j < MAX_CLIENTS - 1; j++) {
                clients[j] = clients[j + 1];
            }

            break;
        }
        if (i == client_count - 1) {
            // Not removing never added client
            return;
        }
    }
    client_count--;

    if (client_count == 0) {
        camera_disable();
        motor_stop();
    }
}

void camera_enable() {
    printf("Starting camera daemon\n");
    system("sudo start-stop-daemon -S --background -m --pidfile /var/run/cameradaemon.pid --exec /opt/mjpgstreamer/mjpg_streamer -- -o \"/opt/mjpgstreamer/output_http.so -w /opt/mjpgstreamer/www\" -i \"/opt/mjpgstreamer/input_raspicam.so -x 240 -y 180 -fps 20\"");
}

void camera_disable() {
    printf("Stopping camera daemon\n");
    system("sudo start-stop-daemon -K --pidfile /var/run/cameradaemon.pid --exec /opt/mjpgstreamer/mjpg_streamer");
}

void setup_gpio() {
    if (wiringPiSetup() < 0) {
        perror("wiringPiSetup");
    }
}
