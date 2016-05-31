#include <stdio.h>
#include "openalpr.hpp"

#ifdef ENABLE_ALPR

#include <stdlib.h>

#include <alpr.h>

static alpr::Alpr *openalpr;

/*int main(){

    alpr_setup();
    struct alpr_packet alpr = alpr_scan("test.png");
    std::cout << alpr.characters << " accuracy: " << alpr.accuracy << "\n";
    alpr_cleanup();
    return 0;

}*/

int alpr_setup() {
    openalpr = new alpr::Alpr("eu", "/etc/openalpr/openalpr.conf");
    openalpr->setTopN(10);
    if (!openalpr->isLoaded()) {
        perror("alpr_setup");
        return 1;
    }

    return 0;
}

struct alpr_packet *alpr_scan(const char *file) {
    alpr::AlprResults results = openalpr->recognize(file);
	if (results.plates.size() != 0){
		alpr::AlprPlateResult plateResult = results.plates[0];
		alpr::AlprPlate plate = plateResult.bestPlate;

		struct alpr_packet *alpr = (alpr_packet *)malloc(sizeof(struct alpr_packet));
		alpr->packet.id = PACKET_ID_ALPR;
        alpr->characters = (const char *)plate.characters.c_str();
		alpr->accuracy = (float)plate.overall_confidence;
		return alpr;
	}else{
        struct alpr_packet *alpr = (alpr_packet *)malloc(sizeof(struct alpr_packet));
        alpr->packet.id = PACKET_ID_ALPR;
        alpr->characters = "No plate";
        alpr->accuracy = 0.0;
        return alpr;
    }

	return NULL;
}

void alpr_cleanup() {
    delete openalpr;
}

#else // No ENABLE_ALPR

int alpr_setup() {
    printf("Alpr is disabled!\n");
    return 0;
}

struct alpr_packet *alpr_scan(const char *file) {
    printf("Alpr is disabled!\n");
    return NULL;
}

void alpr_cleanup() {
}

#endif // ENABLE_ALPR
