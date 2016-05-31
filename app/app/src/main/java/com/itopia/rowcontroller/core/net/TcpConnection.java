package com.itopia.rowcontroller.core.net;

import android.util.Log;

import com.itopia.rowcontroller.AndroidUtils;
import com.itopia.rowcontroller.core.net.packet.Packet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpConnection extends Thread {
    private static final String TAG = "TcpConnection";

    private String address;
    private int port;
    private TcpConnectionCallback callback;

    private Selector selector;
    private SocketChannel socketChannel;

    private PacketSerializer packetSerializer;
    private ByteBuffer inputBuffer;
    private ByteBuffer outputBuffer;

    private AtomicBoolean doDisconnect = new AtomicBoolean(false);

    private final List<Packet> packetQueue = new ArrayList<>();

    public TcpConnection(TcpConnectionCallback callback) {
        this.callback = callback;

        inputBuffer = ByteBuffer.allocate(2 << 12);
        inputBuffer.order(ByteOrder.LITTLE_ENDIAN);
        outputBuffer = ByteBuffer.allocate(2 << 12);
        outputBuffer.order(ByteOrder.LITTLE_ENDIAN);

        packetSerializer = new PacketSerializer();
    }

    public void setAddress(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void disconnect() {
        doDisconnect.set(true);
        selector.wakeup();
    }

    public void run() {
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public void queuePacket(Packet packet) {
        synchronized (packetQueue) {
            if (packet.singleQueue) {
                // Remove all previous packets in the queue that are the same class
                Iterator<Packet> iterator = packetQueue.iterator();
                while (iterator.hasNext()) {
                    Packet item = iterator.next();
                    if (item.getClass().equals(packet.getClass())) {
                        iterator.remove();
                    }
                }
            }

            packetQueue.add(packet);
        }
        selector.wakeup();
    }

    private void connect() throws IOException {
        selector = Selector.open();

        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(address, port));
        socketChannel.register(selector, SelectionKey.OP_CONNECT, null);
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.socket().setSoTimeout(10000);

        List<Packet> internalPacketQueue = new ArrayList<>();

        boolean running = true;
        while (running) {
            if (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    try {
                        processKey(key);
                    } catch (Exception e) {
                        Log.i(TAG, "Fatal exception processing key", e);
                        disconnect();
                    }
                }
            }

            if (doDisconnect.compareAndSet(true, false)) {
                if (socketChannel != null) {
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                        Log.i(TAG, "Exception closing socketChannel", e);
                    }

                    socketChannel = null;

                    Log.i(TAG, "Connection closed");

                    callback(new Runnable() {
                        @Override
                        public void run() {
                            callback.tcpConnectionStateChanged(false);
                        }
                    });

                    running = false;
                }
            } else if (isConnected()) {
                synchronized (packetQueue) {
                    internalPacketQueue.addAll(packetQueue);
                    packetQueue.clear();
                }

                if (internalPacketQueue.size() > 0) {
                    outputBuffer.clear();

                    for (Packet packet : internalPacketQueue) {
                        packetSerializer.writePacket(outputBuffer, packet);
                    }

                    internalPacketQueue.clear();

                    outputBuffer.flip();

                    while (outputBuffer.hasRemaining()) {
                        socketChannel.write(outputBuffer);
                    }
                }
            }
        }
    }

    private void processKey(SelectionKey key) throws IOException {
        if (key.isConnectable()) {
            socketChannel.finishConnect();
            key.interestOps(SelectionKey.OP_READ);

            Log.i(TAG, "Connected!");

            callback(new Runnable() {
                @Override
                public void run() {
                    callback.tcpConnectionStateChanged(true);
                }
            });
        }

        if (key.isReadable()) {
            inputBuffer.limit(inputBuffer.capacity());

            int read;
            while ((read = socketChannel.read(inputBuffer)) > 0) {
            }

            if (read == -1) {
                disconnect();
            } else {
                int bytesRemaining = inputBuffer.position();
                inputBuffer.position(0);
                inputBuffer.limit(bytesRemaining);

                while (bytesRemaining >= 4) {
                    int length = inputBuffer.getInt(0);
                    if (length > 0 && bytesRemaining >= 4 + length) {
                        inputBuffer.position(4);

                        final Packet packet = packetSerializer.readPacket(inputBuffer);
                        callback(new Runnable() {
                            @Override
                            public void run() {
                                callback.receivedTcpPacket(packet);
                            }
                        });

                        bytesRemaining -= inputBuffer.position();

                        // Move the remaining buffer to the front
                        inputBuffer.compact();
                        inputBuffer.position(0);
                        inputBuffer.limit(bytesRemaining);
                    } else {
                        Log.i(TAG, "Waiting for more data length = " + length + " read = " + bytesRemaining);
                        break;
                    }
                }
            }
        }
    }

    public boolean isConnected() {
        return socketChannel != null && socketChannel.isConnected();
    }

    private void callback(Runnable runnable) {
        AndroidUtils.runOnUiThread(runnable);
    }

    public interface TcpConnectionCallback {
        void receivedTcpPacket(Packet packet);

        void tcpConnectionStateChanged(boolean connected);
    }
}
