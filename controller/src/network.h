#ifndef NETWORK_H_
#define NETWORK_H_

#include <sys/poll.h>
#include <stdint.h>
#include <event2/bufferevent.h>

struct network_connection {
    struct event_base *base;

    struct evbuffer *input_evbuffer;
    struct evbuffer *output_evbuffer;

    struct network_buffer* input;
    struct network_buffer* output;
};

#include "protocol.h"

void on_packet(struct network_connection *connection, struct packet *packet);

void network_listen(struct event_base *base);

int network_loop(int server_socket);

void network_accept(evutil_socket_t listener, short events, void *ctx);

void network_event(struct bufferevent *bev, short events, void *ctx);

void network_close(struct network_connection *connection, struct bufferevent *event);

void network_read(struct bufferevent *event, void *ctx);

void network_flush(struct network_connection *connection);

int network_make_socket();

struct network_connection *create_network_connection(struct event_base *base);

void destroy_network_connection(struct network_connection *connection);

#endif
