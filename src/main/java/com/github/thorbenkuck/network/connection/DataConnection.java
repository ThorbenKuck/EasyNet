package com.github.thorbenkuck.network.connection;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public interface DataConnection {

    static DataConnection wrap(Socket socket) throws IOException {
        return new SocketConnection(socket);
    }

    static DataConnection wrap(SocketChannel channel) {
        return new SocketChannelDataConnection(channel);
    }

    static DataConnection wrap(DatagramSocket datagramSocket) {
        return new DatagramSocketConnection(datagramSocket);
    }

    byte[] read(int i) throws IOException;

    void write(byte[] data) throws IOException;

    void flush() throws IOException;

    void close() throws IOException;

    void closeSilent();
}
