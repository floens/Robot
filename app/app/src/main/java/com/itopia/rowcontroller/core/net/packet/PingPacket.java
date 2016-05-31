package com.itopia.rowcontroller.core.net.packet;

import java.nio.ByteBuffer;

public class PingPacket extends Packet {
    public static final int ID = 1;

    public int pingId;

    public PingPacket() {
    }

    public PingPacket(int pingId) {
        this.pingId = pingId;
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putInt(pingId);
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        pingId = buffer.getInt();
    }

    @Override
    public String toString() {
        return "PingPacket{" +
                "pingId=" + pingId +
                '}';
    }
}
