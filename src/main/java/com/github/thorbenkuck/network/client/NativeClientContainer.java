package com.github.thorbenkuck.network.client;

import com.github.thorbenkuck.network.*;
import com.github.thorbenkuck.network.connection.Connection;
import com.github.thorbenkuck.network.stream.EventStream;
import com.github.thorbenkuck.network.stream.SimpleEventStream;
import com.github.thorbenkuck.network.stream.WritableEventStream;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

class NativeClientContainer implements ClientContainer {

	private final Connection connection;
	private final WritableEventStream<RemoteMessage> outStream = new SimpleEventStream<>();
	private ObjectEncoder objectEncoder = new JavaObjectEncoder();
	private ObjectDecoder objectDecoder = new JavaObjectDecoder();

	NativeClientContainer(String address, int port) throws IOException {
		Socket socket = new Socket(address, port);
		connection = Connection.wrap(socket, this::convert);
		connection.outputStream().subscribe(this::handleReceive);
		connection.setOnDisconnect((connection) -> outStream.destroy());
	}

	private byte[] convert(Object o) {
		return objectEncoder.apply(o);
	}

	private void handleReceive(byte[] rawData) {
		Object o = objectDecoder.apply(rawData);
		outStream.push(new RemoteMessage(o, connection));
	}

	@Override
	public EventStream<RemoteMessage> output() {
		return outStream;
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
	public void send(Object o) throws IOException {
		connection.inputStream().push(objectEncoder.apply(o));
	}

	@Override
	public void close() {
		connection.closeSilently();
	}

	@Override
	public void onDisconnect(Consumer<Connection> connectionConsumer) {
		connection.setOnDisconnect(connectionConsumer);
	}

	@Override
	public void listen() {
		connection.listen();
	}
}
