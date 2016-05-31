#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <errno.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <event2/event.h>
#include <event2/buffer.h>
#include <event2/bufferevent.h>
#include <string.h>

#include "network_buffer.h"
#include "motor.h"
#include "distance.h"

#include "controller.h"
#include "openalpr.hpp"
#include "database.h"
#include "sound.h"

/**
 * Process the packet here.
 * The given packet is freed after returning, do not use it afterwards.
 */
void on_packet(struct network_connection *connection, struct packet *packet) {
    switch (packet->id) {
        case PACKET_ID_PING: {
            struct ping_packet *ping = (struct ping_packet *) packet;

            struct pong_packet pong = {
                    .packet.id = PACKET_ID_PONG,
                    .pong_id = ping->ping_id
            };

            protocol_write(connection, (struct packet *) &pong);

            /*struct compass_packet compass = compass_get();

            protocol_write(connection, (struct packet *) &compass);*/

            break;
        }
        case PACKET_ID_MOTOR_COMMAND: {
//            TIMING_START

            struct motor_command_packet *motor_command = (struct motor_command_packet *) packet;
            motor_set(motor_command);

//            TIMING_END("motor_set")

            break;
        }
        case PACKET_ID_DISTANCE_REQUEST: {
            struct distance_request_packet *distance_request = (struct distance_request_packet *) packet;

            float orientation = fmaxf(0.0f, fminf(1.0f, distance_request->orientation));
            distance_set_direction((int) ((1.0f - orientation) * 100.0f));

            break;
        }
        case PACKET_ID_ALPR_REQUEST: {
            printf("Start alpr scan!\n");

            system("wget http://127.0.0.1:8080/?action=snapshot -q -O photo.jpg");
            // ALPR scans photo
            struct alpr_packet *alpr_res = alpr_scan("photo.jpg");

            if (alpr_res != NULL) {
                printf("Alpr scan done!\n");

                int fine = 0;
                char *owner = "None found";

                struct fine_result *fine_result = database_get_fine(alpr_res->characters);
                if (fine_result != NULL) {
                    fine = fine_result->fine;
                    owner = fine_result->owner;
                    free(fine_result);
                }

                alpr_res->fine = fine;
                alpr_res->owner = owner;

                protocol_write(connection, (struct packet *) alpr_res);

                free(alpr_res);
            } else {
                struct alpr_packet alpr_dummy_res = {
                        .packet.id = PACKET_ID_ALPR,
                        .characters = "DUMMY",
                        .accuracy = 100.0f,
                        .fine = 1337,
                        .owner = "Dummy"
                };
                protocol_write(connection, (struct packet *) &alpr_dummy_res);
            }

            break;
        }
        case PACKET_ID_COMMAND: {
            struct command_packet *command_packet = (struct command_packet *) packet;

            char *command = command_packet->command;
            printf("Got command \"%s\"\n", command);

            if (!strcmp(command, "reboot")) {
                system("reboot");
            } else if (!strcmp(command, "shutdown")) {
                system("shutdown -h now");
            } else if (!strcmp(command, "toggle_autoscan")) {
                controller_toggle_autoscanning();
            }

            break;
        }
        case PACKET_ID_SOUND: {
            struct sound_packet *sound_packet = (struct sound_packet *) packet;

            printf("Got play sound %s!\n", sound_packet->name);

            char path[1024];
            sprintf(path, "sounds/%s", sound_packet->name);
            sound_play(path);

            break;
        }
        default:
            break;
    }

    // flush is always called after on_packet
}

void network_listen(struct event_base *base) {
    int server_socket = network_make_socket();
    if (server_socket < 0) {
        perror("network_make_socket");
        goto error;
    }

    if (listen(server_socket, MAX_CLIENTS) < 0) {
        perror("listen");
        goto error;
    }

    struct event *listener_event = event_new(
            base, server_socket, EV_READ | EV_PERSIST, network_accept, (void *) base);

    event_add(listener_event, NULL);

    printf("Accepting connections\n");
    event_base_dispatch(base);

    error:
    exit(1);
}

void network_accept(evutil_socket_t listener, short events, void *ctx) {
    struct event_base *base = ctx;
    struct sockaddr_storage clientaddr;

    socklen_t size = sizeof(clientaddr);
    int fd = accept(listener, (struct sockaddr *) &clientaddr, &size);

    if (fd < 0) {
        perror("accept");
    } else {
        printf("Accepting connection\n");

        struct network_connection *connection = create_network_connection(base);

        evutil_make_socket_nonblocking(fd);
        struct bufferevent *event = bufferevent_socket_new(base, fd, BEV_OPT_CLOSE_ON_FREE);

        bufferevent_setcb(event, network_read, NULL, network_event, connection);
        bufferevent_setwatermark(event, EV_READ, 0, NETWORK_BUFFER_SIZE);
        bufferevent_enable(event, EV_READ | EV_WRITE);

        connection->input_evbuffer = bufferevent_get_input(event);
        connection->output_evbuffer = bufferevent_get_output(event);

        client_connected(connection);
    }
}

void network_event(struct bufferevent *event, short events, void *ctx) {
    struct network_connection *connection = ctx;

//    if (events & (BEV_EVENT_EOF | BEV_EVENT_ERROR)) {
//    } else if (events & BEV_EVENT_TIMEOUT) {
//    }

    network_close(connection, event);
}

void network_close(struct network_connection *connection, struct bufferevent *event) {
    client_disconnected(connection);

    // Also closes the fd
    bufferevent_free(event);
    destroy_network_connection(connection);

    printf("Connection lost\n");
}

void network_read(struct bufferevent *event, void *ctx) {
    struct network_connection *connection = ctx;

    struct network_buffer *input = connection->input;

    if (NETWORK_BUFFER_SIZE - input->size <= 0) {
        printf("buffer full\n"); // TODO errno
        goto close_connection;
    }

    while (1) {
        int bytes_read = evbuffer_remove(
                connection->input_evbuffer, input->buffer + input->size,
                (size_t) (NETWORK_BUFFER_SIZE - input->size));

        if (bytes_read > 0) {
//            printf("read %d bytes\n", bytes_read);

            input->size += bytes_read;
        } else {
            break;
        }
    }

//    TIMING_START
    while (input->size >= 4) {
        // Take a peek of the length
        uint32_t length = peek_uint(input);

        if (length > 0 && input->size >= 4 + length) {
            input->position += 4;
            struct packet *packet = protocol_read(connection);

            if (errno == EOF) {
                errno = 0;
                printf("reached EOF reading packet!\n");
                goto close_connection;
            }

            if (packet != NULL) {
                on_packet(connection, packet);
                packet_free(packet);
            } else {
                printf("protocol_read returned NULL\n");
                goto close_connection;
            }

            network_buffer_compact(input);
        } else {
            printf("Waiting for more data until %d bytes have been received, received %d bytes now\n", length,
                   input->size - 4);
            break;
        }
    }
//    TIMING_END("network input handling")

    network_flush(connection);

    return;

    close_connection:
    network_close(connection, event);
}

void network_flush(struct network_connection *connection) {
    struct network_buffer *output = connection->output;

    if (output->position > 0) {
//        printf("writing %d bytes\n", output->position);

        network_buffer_flip(output);

        // Write buffer
        if (evbuffer_add(connection->output_evbuffer,
                         (output->buffer + output->position),
                         output->size - output->position) < 0) {
            printf("evbuffer_add fail");
        }

        network_buffer_clear(output);
    }
}

int network_make_socket() {
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock == -1) {
        return -1;
    }

    struct sockaddr_in address = {
            .sin_family = AF_INET,
            .sin_port = htons(PORT),
            .sin_addr.s_addr = INADDR_ANY
    };

    if (evutil_make_socket_nonblocking(sock) < 0) {
        return -1;
    }

    int enable = 1;
    if (setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int)) < 0) {
        perror("setsockopt");
    }

    if (bind(sock, (struct sockaddr *) &address, sizeof(address)) < 0) {
        return -1;
    }

    return sock;
}

struct network_connection *create_network_connection(struct event_base *base) {
    struct network_connection *connection = malloc(sizeof(struct network_connection));
    connection->base = base;

    connection->input_evbuffer = NULL;
    connection->output_evbuffer = NULL;

    connection->input = create_network_buffer();
    connection->input->size = 0;
    connection->output = create_network_buffer();

    return connection;
}

void destroy_network_connection(struct network_connection *connection) {
    destroy_network_buffer(connection->input);
    destroy_network_buffer(connection->output);
    free(connection);
}
