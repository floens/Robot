package com.itopia.rowcontroller.core.net.packet;

import java.nio.ByteBuffer;

public class MotorCommandPacket extends Packet {
    public static final int ID = 4;

    public float speed;
    public float direction;

    public MotorCommandPacket() {
        singleQueue = true;
    }

    public MotorCommandPacket(float speed, float direction) {
        this();
        this.speed = speed;
        this.direction = direction;
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putFloat(speed);
        buffer.putFloat(direction);
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        speed = buffer.getFloat();
        direction = buffer.getFloat();
    }
}
