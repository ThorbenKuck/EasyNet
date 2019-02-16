package com.github.thorbenkuck.network.connection;

import com.github.thorbenkuck.network.NativeSession;
import com.github.thorbenkuck.network.RawData;
import com.github.thorbenkuck.network.Session;
import com.github.thorbenkuck.network.stream.EventStream;
import com.github.thorbenkuck.network.stream.DataStream;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Connection {

	static Connection wrap(Socket socket, Session session) throws IOException {
		TCPConnection tcpConnection = new TCPConnection(socket);
		tcpConnection.setSession(session);

		return tcpConnection;
	}

	static Connection wrap(Socket socket, Function<Object, byte[]> converter) throws IOException {
		TCPConnection tcpConnection = new TCPConnection(socket);
		Session session = new NativeSession(tcpConnection, converter);
		tcpConnection.setSession(session);

		return tcpConnection;
	}

	DataStream<byte[]> inputStream();

	void setSession(Session session);

	void listen();

	void writeSilent(byte[] data);

	void writeSilent(RawData data);

	void write(byte[] data) throws IOException;

	void write(RawData data) throws IOException;

	EventStream<byte[]> outputStream();

	void closeSilently();

	void close() throws IOException;

	Session session();

	String remoteAddress();

	Consumer<Connection> getOnDisconnect();

	void setOnDisconnect(Consumer<Connection> onDisconnect);
}
