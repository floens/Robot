package com.itopia.rowcontroller.core.net;

import com.itopia.rowcontroller.core.net.packet.AlprPacket;
import com.itopia.rowcontroller.core.net.packet.AlprRequestPacket;
import com.itopia.rowcontroller.core.net.packet.CompassPacket;
import com.itopia.rowcontroller.core.net.packet.DistancePacket;
import com.itopia.rowcontroller.core.net.packet.DistanceRequestPacket;
import com.itopia.rowcontroller.core.net.packet.MotorCommandPacket;
import com.itopia.rowcontroller.core.net.packet.Packet;
import com.itopia.rowcontroller.core.net.packet.PingPacket;
import com.itopia.rowcontroller.core.net.packet.PongPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class PacketSerializer {
    private static final Map<Integer, Class<? extends Packet>> registeredPackets = new HashMap<>();

    static {
        registeredPackets.put(PingPacket.ID, PingPacket.class);
        registeredPackets.put(PongPacket.ID, PongPacket.class);
        registeredPackets.put(MotorCommandPacket.ID, MotorCommandPacket.class);
        registeredPackets.put(DistancePacket.ID, DistancePacket.class);
        registeredPackets.put(DistanceRequestPacket.ID, DistanceRequestPacket.class);
        registeredPackets.put(CompassPacket.ID, CompassPacket.class);
        registeredPackets.put(AlprRequestPacket.ID, AlprRequestPacket.class);
        registeredPackets.put(AlprPacket.ID, AlprPacket.class);
    }

    private ByteBuffer outputBuffer;

    public PacketSerializer() {
        outputBuffer = ByteBuffer.allocate(2 << 12);
        outputBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public Packet readPacket(ByteBuffer buffer) throws IOException {
        int id = buffer.getInt();

        Class<? extends Packet> packetClass = registeredPackets.get(id);
        if (packetClass == null) {
            throw new ProtocolException("Unknown packet id = " + id);
        }

        Packet packet;
        try {
            packet = packetClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        packet.deserialize(buffer);

        return packet;
    }

    public void writePacket(ByteBuffer buffer, Packet packet) throws IOException {
        outputBuffer.clear();
        outputBuffer.putInt(packet.id());
        packet.serialize(outputBuffer);

        int length = outputBuffer.position();
        buffer.putInt(length);

        outputBuffer.flip();
        buffer.put(outputBuffer);
    }
}
