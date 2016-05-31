package com.itopia.rowcontroller.core.net.packet;

import java.nio.ByteBuffer;

public class DistancePacket extends Packet {
    public static final int ID = 5;

    public int distance;
    public int autotune;
    public float position;

    @Override
    public int id() {
        return ID;
    }

    public DistancePacket() {
    }

    public DistancePacket(int distance) {
        this.distance = distance;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putInt(distance);
        buffer.putInt(autotune);
        buffer.putFloat(position);
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        distance = buffer.getInt();
        autotune = buffer.getInt();
        position = buffer.getFloat();
    }

    @Override
    public String toString() {
        return "DistancePacket{" +
                "distance=" + distance +
                ", autotune=" + autotune +
                ", position=" + position +
                '}';
    }
}
