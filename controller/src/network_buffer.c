#include <stdlib.h>
#include <string.h>
#include <sys/errno.h>
#include <stdio.h>
#include <stdint.h>

#include "network_buffer.h"

struct network_buffer *create_network_buffer() {
    struct network_buffer *buffer = malloc(sizeof(struct network_buffer));
    buffer->buffer = malloc(NETWORK_BUFFER_SIZE);
    network_buffer_clear(buffer);
    return buffer;
}

void destroy_network_buffer(struct network_buffer *buffer) {
    free(buffer->buffer);
    free(buffer);
}

void network_buffer_flip(struct network_buffer *buffer) {
    buffer->size = buffer->position;
    buffer->position = 0;
}

void network_buffer_clear(struct network_buffer *buffer) {
    buffer->size = NETWORK_BUFFER_SIZE;
    buffer->position = 0;
}

void network_buffer_compact(struct network_buffer *buffer) {
    unsigned int position = buffer->position;
    memmove(buffer->buffer, buffer->buffer + position, buffer->size - position);
    buffer->position = 0;
    buffer->size -= position;
}

void network_buffer_append(struct network_buffer *buffer, struct network_buffer *append) {
    size_t length = append->size;
    memcpy(buffer->buffer + buffer->position, append->buffer, length);
    buffer->position += length;
}

int32_t read_int(struct network_buffer *buffer) {
    if (buffer->position + 4 > buffer->size) {
        errno = EOF;
        return 0;
    }

    int32_t value;
    memcpy(&value, buffer->buffer + buffer->position, 4);
    buffer->position += 4;
    return value;
}

uint32_t peek_uint(struct network_buffer *buffer) {
    if (buffer->position + 4 > buffer->size) {
        errno = EOF;
        return 0;
    }

    uint32_t value;
    memcpy(&value, buffer->buffer + buffer->position, 4);
    return value;
}

uint32_t read_uint(struct network_buffer *buffer) {
    if (buffer->position + 4 > buffer->size) {
        errno = EOF;
        return 0;
    }

    uint32_t value;
    memcpy(&value, buffer->buffer + buffer->position, 4);
    buffer->position += 4;
    return value;
}

uint16_t read_ushort(struct network_buffer *buffer) {
    if (buffer->position + 2 > buffer->size) {
        errno = EOF;
        return 0;
    }

    uint16_t value;
    memcpy(&value, buffer->buffer + buffer->position, 2);
    buffer->position += 2;
    return value;
}

uint8_t read_ubyte(struct network_buffer *buffer) {
    if (buffer->position + 1 > buffer->size) {
        errno = EOF;
        return 0;
    }

    uint8_t value;
    memcpy(&value, buffer->buffer + buffer->position, 1);
    buffer->position += 1;
    return value;
}

float read_float(struct network_buffer *buffer) {
    if (buffer->position + 4 > buffer->size) {
        errno = EOF;
        return 0;
    }

    float value;
    memcpy(&value, buffer->buffer + buffer->position, 4);
    buffer->position += 4;
    return value;
}

char *read_string(struct network_buffer *buffer) {
    uint16_t length = read_ushort(buffer);
    if (errno == EOF) {
        return NULL;
    }

    char *string = malloc(length + 1);
    for (int i = 0; i < length; i++) {
        string[i] = read_ubyte(buffer);
        if (errno == EOF) {
            return NULL;
        }
    }
    string[length] = 0;
    return string;
}

void write_int(struct network_buffer *buffer, int32_t value) {
    if (buffer->position + 4 > buffer->size) {
        errno = EOF;
        return;
    }

    memcpy(buffer->buffer + buffer->position, &value, 4);
    buffer->position += 4;
}

void write_uint(struct network_buffer *buffer, uint32_t value) {
    if (buffer->position + 4 > buffer->size) {
        errno = EOF;
        return;
    }

    memcpy(buffer->buffer + buffer->position, &value, 4);
    buffer->position += 4;
}

void write_ushort(struct network_buffer *buffer, uint16_t value) {
    if (buffer->position + 2 > buffer->size) {
        errno = EOF;
        return;
    }

    memcpy(buffer->buffer + buffer->position, &value, 2);
    buffer->position += 2;
}

void write_ubyte(struct network_buffer *buffer, uint8_t value) {
    if (buffer->position + 1 > buffer->size) {
        errno = EOF;
        return;
    }

    memcpy(buffer->buffer + buffer->position, &value, 1);
    buffer->position += 1;
}

void write_float(struct network_buffer *buffer, float value) {
    if (buffer->position + 4 > buffer->size) {
        printf("eof\n");
        errno = EOF;
        return;
    }

    memcpy(buffer->buffer + buffer->position, &value, 4);
    buffer->position += 4;
}

void write_string(struct network_buffer *buffer, const char *string, uint16_t length) {
    write_ushort(buffer, length);
    for (int i = 0; i < length; i++) {
        write_ubyte(buffer, (uint8_t) string[i]);
    }
}
