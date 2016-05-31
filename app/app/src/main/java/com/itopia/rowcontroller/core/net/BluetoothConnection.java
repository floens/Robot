package com.itopia.rowcontroller.core.net;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnection {
    private static final String TAG = "BluetoothConnection";

    private BluetoothSocket socket;

    public BluetoothConnection() {
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void connect() throws IOException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IllegalArgumentException("Device has no bluetooth");
        }

        if (!adapter.isEnabled()) {
            throw new IllegalArgumentException("Bluetooth disabled");
        }

        BluetoothDevice remoteDevice = adapter.getRemoteDevice("00:00:00:00:00:00");

        if (remoteDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            throw new IllegalArgumentException("Not paired to bluetooth device");
        }

        socket = remoteDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

        socket.connect();
        OutputStream outputStream = socket.getOutputStream();

        outputStream.write("Hello, world!\n".getBytes());
    }

    public void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
    }
}
