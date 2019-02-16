package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.*;
import com.github.thorbenkuck.network.connection.Connection;
import com.github.thorbenkuck.network.stream.EventStream;
import com.github.thorbenkuck.network.stream.SimpleEventStream;
import com.github.thorbenkuck.network.stream.WritableEventStream;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

class NativeServerContainer implements ServerContainer {

	private final WritableEventStream<Connection> connected = new SimpleEventStream<>();
	private final ServerSocket serverSocket;
	private final AtomicBoolean accepting = new AtomicBoolean(false);
	private final WritableEventStream<RemoteMessage> outStream = new SimpleEventStream<>();
	private ObjectEncoder objectEncoder = new JavaObjectEncoder();
	private ObjectDecoder objectDecoder = new JavaObjectDecoder();

	NativeServerContainer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		connected.subscribe(this::newConnection);
	}

	private byte[] convert(Object object) {
		return objectEncoder.apply(object);
	}

	private void newConnection(Connection connection) {
		connection.outputStream().subscribe(rawData -> receivedData(rawData, connection));
		connection.listen();
	}

	private void receivedData(byte[] rawData, Connection connection) {
		Object o = objectDecoder.apply(rawData);
		outStream.push(new RemoteMessage(o, connection));
	}

	@Override
	public EventStream<RemoteMessage> output() {
		return outStream;
	}

	@Override
	public void accept() {
		accepting.set(true);
		while (accepting.get()) {
			try {
				Socket socket = serverSocket.accept();
				connected.push(Connection.wrap(socket, this::convert));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public EventStream<Connection> ingoingConnections() {
		return connected;
	}

	@Override
	public ObjectEncoder getObjectEncoder() {
		return objectEncoder;
	}

	@Override
	public void setObjectEncoder(ObjectEncoder objectEncoder) {
		this.objectEncoder = objectEncoder;
	}

	@Override
	public ObjectDecoder getObjectDecoder() {
		return objectDecoder;
	}

	@Override
	public void setObjectDecoder(ObjectDecoder objectDecoder) {
		this.objectDecoder = objectDecoder;
	}

	@Override
	public void close() throws IOException {
		serverSocket.close();
		outStream.destroy();
	}

	@Override
	public void closeSilently() {
		try {
			serverSocket.close();
		} catch (IOException ignored) {
		}

		outStream.destroy();
	}
}
