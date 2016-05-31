package com.itopia.rowcontroller.core.net.packet;

import java.nio.ByteBuffer;

public class DistanceRequestPacket extends Packet {
    public static final int ID = 6;

    public float orientation;

    public DistanceRequestPacket() {
    }

    public DistanceRequestPacket(float orientation) {
        this.orientation = orientation;
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putFloat(orientation);
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        orientation = buffer.getFloat();
    }
}
