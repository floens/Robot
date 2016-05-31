#ifndef NETWORK_BUFFER_H
#define NETWORK_BUFFER_H

#define NETWORK_BUFFER_SIZE (1 << 12)

struct network_buffer {
    void *buffer;
    unsigned int size;
    unsigned int position;
};

struct network_buffer *create_network_buffer();

void destroy_network_buffer(struct network_buffer *buffer);

void network_buffer_flip(struct network_buffer *buffer);

void network_buffer_clear(struct network_buffer *buffer);

void network_buffer_compact(struct network_buffer *buffer);

void network_buffer_append(struct network_buffer *buffer, struct network_buffer *append);

int32_t read_int(struct network_buffer *buffer);

uint32_t peek_uint(struct network_buffer *buffer);

uint32_t read_uint(struct network_buffer *buffer);

uint16_t read_ushort(struct network_buffer *buffer);

uint8_t read_ubyte(struct network_buffer *buffer);

float read_float(struct network_buffer *buffer);

char *read_string(struct network_buffer *buffer);

void write_int(struct network_buffer *buffer, int32_t value);

void write_uint(struct network_buffer *buffer, uint32_t value);

void write_ushort(struct network_buffer *buffer, uint16_t value);

void write_ubyte(struct network_buffer *buffer, uint8_t value);

void write_float(struct network_buffer *buffer, float value);

void write_string(struct network_buffer *buffer, const char *string, uint16_t length);

#endif
