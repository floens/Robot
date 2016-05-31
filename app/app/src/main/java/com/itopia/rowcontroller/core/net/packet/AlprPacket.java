package com.itopia.rowcontroller.core.net.packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AlprPacket extends Packet {
    public static final int ID = 9;

    public String characters;
    public float accuracy;
    public int fine;
    public String owner;

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void serialize(ByteBuffer buffer) throws IOException {
    }

    @Override
    public void deserialize(ByteBuffer buffer) throws IOException {
        characters = readString(buffer);
        accuracy = buffer.getFloat();
        fine = buffer.getInt();
        owner = readString(buffer);
    }
}
