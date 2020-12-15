package com.github.thorbenkuck.network.connection;

import java.net.DatagramSocket;
import java.net.SocketAddress;

class UdpConnection extends AbstractConnection {

    private final DataConnection dataConnection;
    private final DatagramSocket datagramSocket;

    UdpConnection(DatagramSocket datagramSocket) {
        dataConnection = DataConnection.wrap(datagramSocket);
        this.datagramSocket = datagramSocket;
        setProtocol(new SizeFirstProtocol());
        pipeInputStreams();
    }

    @Override
    public DataConnection getDataConnection() {
        return dataConnection;
    }

    @Override
    public void listen() {
    }

    @Override
    public SocketAddress remoteAddress() {
        return datagramSocket.getRemoteSocketAddress();
    }

    @Override
    public SocketAddress localAddress() {
        return datagramSocket.getLocalSocketAddress();
    }

    @Override
    public boolean isOpen() {
        return !datagramSocket.isClosed() && datagramSocket.isBound();
    }

    @Override
    public void closeSilently() {
        dataConnection.closeSilent();
        datagramSocket.close();
    }
}
