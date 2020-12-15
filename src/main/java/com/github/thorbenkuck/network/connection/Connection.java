package com.github.thorbenkuck.network.connection;

import com.github.thorbenkuck.network.stream.DataStream;
import com.github.thorbenkuck.network.stream.EventStream;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

public interface Connection {

    static Connection wrap(Socket socket) throws IOException {
        return new BlockingConnection(socket);
    }

    static Connection wrap(SocketChannel socketChannel) {
        return new NonBlockingConnection(socketChannel);
    }

    static Connection wrap(DatagramSocket datagramSocket) {
        return new UdpConnection(datagramSocket);
    }

    void listen();

    Protocol getProtocol();

    void setProtocol(Protocol protocol);

    byte[] readFromProtocol() throws IOException;

    void writeToProtocol(byte[] data) throws IOException;

	EventStream<byte[]> output();

	DataStream<byte[]> input();

	EventStream<String> systemOutput();

	DataStream<String> systemInput();

	SocketAddress remoteAddress();

	SocketAddress localAddress();

	Consumer<Connection> getOnDisconnect();

	void setOnDisconnect(Consumer<Connection> onDisconnect);

	boolean isOpen();

	void closeSilently();

	void close() throws IOException;

    void pauseOutput();

    void unpauseOutput();
}
