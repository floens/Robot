package com.itopia.rowcontroller.core.net.packet;

import java.nio.ByteBuffer;

public class AlprRequestPacket extends Packet {
    public static final int ID = 8;

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
    }
}
