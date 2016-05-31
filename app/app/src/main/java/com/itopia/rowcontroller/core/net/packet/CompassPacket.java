package com.itopia.rowcontroller.core.net.packet;

import java.nio.ByteBuffer;

public class CompassPacket extends Packet {
    public static final int ID = 7;

    public float x;
    public float y;
    public float z;
    public float angle;
    public float magnitude;

    public CompassPacket() {
    }

    public CompassPacket(float x, float y, float z, float angle, float magnitude) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = angle;
        this.magnitude = magnitude;
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.putFloat(angle);
        buffer.putFloat(magnitude);
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        x = buffer.getFloat();
        y = buffer.getFloat();
        z = buffer.getFloat();
        angle = buffer.getFloat();
        magnitude = buffer.getFloat();
    }

    @Override
    public String toString() {
        return "CompassPacket{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", angle=" + angle +
                ", magnitude=" + magnitude +
                '}';
    }
}
