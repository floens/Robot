package com.itopia.rowcontroller.core.net.packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CommandPacket extends Packet {
    public static final int ID = 10;

    public String command;

    public CommandPacket() {
    }

    public CommandPacket(String command) {
        this.command = command;
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void serialize(ByteBuffer buffer) throws IOException {
        writeString(buffer, command);
    }

    @Override
    public void deserialize(ByteBuffer buffer) throws IOException {
        command = readString(buffer);
    }
}
