#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <event2/buffer.h>
#include <string.h>

#include "network_buffer.h"

#include "protocol.h"

struct packet *protocol_read(struct network_connection *connection) {
    struct network_buffer *input = connection->input;

    uint32_t id = read_uint(input);

    struct packet *packet = NULL;

    switch (id) {
        case PACKET_ID_PING: {
            struct ping_packet *ping = malloc(sizeof(struct ping_packet));

            ping->ping_id = read_uint(input);

            packet = (struct packet *) ping;
            break;
        }
        case PACKET_ID_PONG: {
            struct pong_packet *pong = malloc(sizeof(struct pong_packet));

            pong->pong_id = read_uint(input);

            packet = (struct packet *) pong;
            break;
        }
        case PACKET_ID_MOTOR_COMMAND: {
            struct motor_command_packet *motor_command = malloc(sizeof(struct motor_command_packet));

            motor_command->speed = read_float(input);
            motor_command->direction = read_float(input);

            packet = (struct packet *) motor_command;
            break;
        }
        case PACKET_ID_DISTANCE_REQUEST: {
            struct distance_request_packet *distance_request = malloc(sizeof(struct distance_request_packet));

            distance_request->orientation = read_float(input);

            packet = (struct packet *) distance_request;
            break;
        }
        case PACKET_ID_ALPR_REQUEST: {
            struct alpr_req_packet *alpr_request = malloc(sizeof(struct alpr_req_packet));
            packet = (struct packet *) alpr_request;
            break;
        }
        case PACKET_ID_COMMAND: {
            struct command_packet *command_packet = malloc(sizeof(struct command_packet));

            command_packet->command = read_string(input);

            packet = (struct packet *) command_packet;
            break;
        }
        case PACKET_ID_SOUND: {
            struct sound_packet *sound_packet = malloc(sizeof(struct sound_packet));

            sound_packet->name = read_string(input);

            packet = (struct packet *) sound_packet;
            break;
        }
        default:
            printf("Received a unknown packet id = %d\n", id);
            break;
    }

    if (packet != NULL) {
        packet->id = id;
    }

    return packet;
}

static struct network_buffer *output_buffer = NULL;

void protocol_write(struct network_connection *connection, struct packet *packet) {
    if (output_buffer == NULL) {
        output_buffer = create_network_buffer();
    }

    write_uint(output_buffer, packet->id);

    switch (packet->id) {
        case PACKET_ID_PING: {
            struct ping_packet *ping = (struct ping_packet *) packet;

            write_uint(output_buffer, ping->ping_id);

            break;
        }
        case PACKET_ID_PONG: {
            struct pong_packet *pong = (struct pong_packet *) packet;

            write_uint(output_buffer, pong->pong_id);

            break;
        }
        case PACKET_ID_DISTANCE: {
            struct distance_packet *distance = (struct distance_packet *) packet;

            write_uint(output_buffer, distance->distance);
            write_uint(output_buffer, distance->autotune);
            write_float(output_buffer, distance->position);

            break;
        }
        case PACKET_ID_COMPASS: {
            struct compass_packet *compass = (struct compass_packet *) packet;

            write_float(output_buffer, compass->x);
            write_float(output_buffer, compass->y);
            write_float(output_buffer, compass->z);
            write_float(output_buffer, compass->angle);
            write_float(output_buffer, compass->magnitude);

            break;
        }
        case PACKET_ID_ALPR: {
            printf("writing alpr response\n");

            struct alpr_packet *alpr = (struct alpr_packet *) packet;

            write_string(output_buffer, alpr->characters, (uint32_t) strlen(alpr->characters));
            write_float(output_buffer, alpr->accuracy);
            write_int(output_buffer, alpr->fine);
            write_string(output_buffer, alpr->owner, (uint32_t) strlen(alpr->owner));

            break;
        }
        default:
            printf("Cannot send unknown packet id = %d\n", packet->id);
            break;
    }

    network_buffer_flip(output_buffer);

    write_uint(connection->output, output_buffer->size);

    network_buffer_append(connection->output, output_buffer);
    network_buffer_clear(output_buffer);
}

void packet_free(struct packet *packet) {
    free(packet);
}
