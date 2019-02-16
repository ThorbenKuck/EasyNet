package com.github.thorbenkuck.network.client;

import com.github.thorbenkuck.network.ObjectDecoder;
import com.github.thorbenkuck.network.ObjectEncoder;
import com.github.thorbenkuck.network.RemoteMessage;
import com.github.thorbenkuck.network.connection.Connection;
import com.github.thorbenkuck.network.stream.EventStream;

import java.io.IOException;
import java.util.function.Consumer;

public interface ClientContainer {

	static ClientContainer open(String address, int port) throws IOException {
		return new NativeClientContainer(address, port);
	}

	EventStream<RemoteMessage> output();

	ObjectEncoder getObjectEncoder();

	void setObjectEncoder(ObjectEncoder objectEncoder);

	ObjectDecoder getObjectDecoder();

	void setObjectDecoder(ObjectDecoder objectDecoder);

	void send(Object o) throws IOException;

	void close();

	void onDisconnect(Consumer<Connection> connectionConsumer);

	void listen();
}
