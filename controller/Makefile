export LANG=en
CC=gcc
CPP=g++
CFLAGS=-c -Wall -g -O2

INCLUDE=-lz -lm -ldl -lbluetooth -lwiringPi -lwiringPiDev -lpthread -levent -I/usr/include/mysql -L/usr/lib/arm-linux-gnueabihf -lmysqlclient -lmpg123 -lao

INCLUDE+=-lopenalpr
APLR_FLAG=-DENABLE_ALPR
#APLR_FLAG=

WITH_PWM_FLAG=-DENABLE_PWM
#WITH_PWM_FLAG=

EXEC=controller_server
SRC=$(wildcard src/*.c) $(wildcard src/*.cpp)
OBJ=$(patsubst %.c,%.o,$(wildcard src/*.c)) $(patsubst %.cpp,%.o,$(wildcard src/*.cpp))

all: $(SRC) $(EXEC)

$(EXEC): $(OBJ)
	$(CPP) $(OBJ) $(INCLUDE) -o $@

.c.o:
	$(CC) $(CFLAGS) -std=c1x $(INCLUDE) $< -o $@ $(APLR_FLAG) $(WITH_PWM_FLAG)

.cpp.o:
	$(CPP) $(CFLAGS) -std=c++11 $(INCLUDE) $< -o $@ $(APLR_FLAG) $(WITH_PWM_FLAG)

clean:
	rm -f $(OBJ)
	rm -f $(EXEC)

run: all
	sudo ./$(EXEC)
