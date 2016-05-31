package com.itopia.rowcontroller.core.net;

import android.util.Log;

import com.itopia.rowcontroller.AndroidUtils;
import com.itopia.rowcontroller.core.net.packet.AlprPacket;
import com.itopia.rowcontroller.core.net.packet.CompassPacket;
import com.itopia.rowcontroller.core.net.packet.DistancePacket;
import com.itopia.rowcontroller.core.net.packet.Packet;
import com.itopia.rowcontroller.core.net.packet.PingPacket;
import com.itopia.rowcontroller.core.net.packet.PongPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RobotConnection implements TcpConnection.TcpConnectionCallback {
    private static final String TAG = "RobotConnection";

    private final Callback callback;
    private String host;
    private int port;

    private Random random;

    private BluetoothConnection bluetoothConnection;
    private TcpConnection tcpConnection;

    private Map<Integer, Long> pongTimes = new HashMap<>();
    private long lastPongTime;

    public RobotConnection(Callback callback) {
        this.callback = callback;

        random = new Random();
    }

    public void setTcpAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        if (host.length() > 0 && port > 0 && port < 65536 && !isConnected()) {
            tcpConnection = new TcpConnection(this);
            tcpConnection.setAddress(host, port);
            tcpConnection.start();
        }
    }

    public void disconnect() {
        if (tcpConnection != null) {
            tcpConnection.disconnect();
            tcpConnection = null;
        }
    }

    public boolean isConnected() {
        return tcpConnection != null && tcpConnection.isConnected();
    }

    public void queuePacket(Packet packet) {
        if (tcpConnection != null && tcpConnection.isConnected()) {
            tcpConnection.queuePacket(packet);
        }
    }

    public Map<Integer, Long> getPongTimes() {
        return pongTimes;
    }

    public long getLastPongTime() {
        return lastPongTime;
    }

    @Override
    public void receivedTcpPacket(Packet packet) {
//        Log.i(TAG, "Received packet = " + packet.toString());

        switch (packet.id()) {
            case PongPacket.ID: {
                PongPacket ping = (PongPacket) packet;

                Long time = pongTimes.get(ping.pongId);

                if (time != null) {
                    pongTimes.remove(ping.pongId);
                    lastPongTime = System.currentTimeMillis() - time;

//                    callback.showStatus("Pong " + lastPongTime + "ms");
                } else {
                    Log.w(TAG, "Pong id invalid");
                    tcpConnection.disconnect();
                }

                break;
            }
            case DistancePacket.ID: {
                DistancePacket distance = (DistancePacket) packet;

                callback.onDistanceResult(distance);
                break;
            }
            case CompassPacket.ID: {
                CompassPacket compass = (CompassPacket) packet;

                callback.setCompass(compass);

//                callback.showStatus("x = " + compass.x + ", y = " + compass.y + ", z = " + compass.z);

                break;
            }
            case AlprPacket.ID: {
                callback.onAlprResult((AlprPacket) packet);

                break;
            }
        }
    }

    @Override
    public void tcpConnectionStateChanged(boolean connected) {
        if (connected) {
            callback.showCamera(true, "http://" + host + ":8080/?action=stream");
            pingLoop();
        } else {
            callback.showCamera(false, null);
            pongTimes.clear();
            lastPongTime = 0;
        }

        callback.tcpConnected(connected);
    }

    private void pingLoop() {
        if (tcpConnection != null && tcpConnection.isConnected()) {
            if (pongTimes.size() == 0) {
                int id = random.nextInt();
                pongTimes.put(id, System.currentTimeMillis());
                tcpConnection.queuePacket(new PingPacket(id));
            }

            AndroidUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pingLoop();
                }
            }, 1000 / 2);
        }
    }

    public interface Callback {
        void tcpConnected(boolean connected);

        void showStatus(String status);

        void setCompass(CompassPacket packet);

        void showCamera(boolean show, String url);

        void onAlprResult(AlprPacket packet);

        void onDistanceResult(DistancePacket packet);
    }
}
