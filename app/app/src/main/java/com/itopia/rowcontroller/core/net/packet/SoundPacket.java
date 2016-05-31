package com.itopia.rowcontroller.core.net.packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SoundPacket extends Packet {
    public static final int ID = 11;

    public String sound;

    public SoundPacket(String sound) {
        this.sound = sound;
    }

    public SoundPacket() {
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void serialize(ByteBuffer buffer) throws IOException {
        writeString(buffer, sound);
    }

    @Override
    public void deserialize(ByteBuffer buffer) throws IOException {
    }
}
