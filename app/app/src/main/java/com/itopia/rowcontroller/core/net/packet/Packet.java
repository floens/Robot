package com.itopia.rowcontroller.core.net.packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class Packet {
    public boolean singleQueue = false;

    public abstract int id();

    public abstract void serialize(ByteBuffer buffer) throws IOException;

    public abstract void deserialize(ByteBuffer buffer) throws IOException;

    public void writeString(ByteBuffer buffer, String value) throws IOException {
        if (value.length() > Short.MAX_VALUE) {
            throw new IOException("Invalid string length");
        }

        buffer.putShort((short) value.length());
        for (int i = 0; i < value.length(); i++) {
            buffer.put((byte) value.charAt(i));
        }
    }

    public String readString(ByteBuffer buffer) throws IOException {
        short length = buffer.getShort();
        if (length >= 0) {
            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                chars[i] = (char) buffer.get();
            }
            return new String(chars);
        } else {
            throw new IOException("Invalid string length");
        }
    }
}
