package com.itopia.rowcontroller.core.net.packet;

import java.nio.ByteBuffer;

public class PongPacket extends Packet {
    public static final int ID = 2;

    public int pongId;

    public PongPacket() {
    }

    public PongPacket(int pongId) {
        this.pongId = pongId;
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putInt(pongId);
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        pongId = buffer.getInt();
    }

    @Override
    public String toString() {
        return "PongPacket{" +
                "pongId=" + pongId +
                '}';
    }
}
