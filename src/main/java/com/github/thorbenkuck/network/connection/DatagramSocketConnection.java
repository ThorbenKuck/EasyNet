package com.github.thorbenkuck.network.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

class DatagramSocketConnection implements DataConnection {

    private final DatagramSocket datagramSocket;

    DatagramSocketConnection(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    @Override
    public byte[] read(int i) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(new byte[i], i);
        datagramSocket.receive(datagramPacket);
        return datagramPacket.getData();
    }

    @Override
    public void write(byte[] data) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
        datagramPacket.setAddress(((InetSocketAddress) datagramSocket.getRemoteSocketAddress()).getAddress());
        datagramPacket.setPort(datagramSocket.getPort());
        datagramSocket.send(datagramPacket);
    }

    @Override
    public void flush() {
        // Ignored
    }

    @Override
    public void close() {
        datagramSocket.close();
    }

    @Override
    public void closeSilent() {
        datagramSocket.close();
    }
}
