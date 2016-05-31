#ifndef OPENALPRCONTROLLER_H_
#define OPENALPRCONTROLLER_H_

#include "protocol.h"

#ifdef __cplusplus
extern "C" {
#endif

int alpr_setup();

struct alpr_packet *alpr_scan(const char *file);

void alpr_cleanup();

#ifdef __cplusplus
}
#endif

#endif // OPENALPRCONTROLLER_H_
