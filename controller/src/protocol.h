#ifndef PROTOCOL_H_
#define PROTOCOL_H_

#include <stdint.h>

#define PACKET_ID_PING 1
#define PACKET_ID_PONG 2
#define PACKET_ID_MOTOR_COMMAND 4
#define PACKET_ID_DISTANCE 5
#define PACKET_ID_DISTANCE_REQUEST 6
#define PACKET_ID_COMPASS 7
#define PACKET_ID_ALPR_REQUEST 8
#define PACKET_ID_ALPR 9
#define PACKET_ID_COMMAND 10
#define PACKET_ID_SOUND 11

struct packet {
    uint32_t id;
};

struct ping_packet {
    struct packet packet;
    uint32_t ping_id;
};

struct pong_packet {
    struct packet packet;
    uint32_t pong_id;
};

struct motor_command_packet {
    struct packet packet;
    float speed;
    float direction;
};

struct distance_packet {
    struct packet packet;
    uint32_t distance;
    uint32_t autotune;
    float position;
};

struct distance_request_packet {
    struct packet packet;
    float orientation;
};

struct compass_packet {
    struct packet packet;
    float x;
    float y;
    float z;
    float angle;
    float magnitude;
};

struct alpr_req_packet {
    struct packet packet;
};

struct alpr_packet {
    struct packet packet;
    const char *characters;
    float accuracy;
    int fine;
    char *owner;
};

struct command_packet {
    struct packet packet;
    char *command;
};

struct sound_packet {
    struct packet packet;
    char *name;
};

#include "network.h"

struct packet *protocol_read(struct network_connection *connection);

void protocol_write(struct network_connection *connection, struct packet *packet);

void packet_free(struct packet *packet);

#endif
