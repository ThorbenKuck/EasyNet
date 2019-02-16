package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.ObjectDecoder;
import com.github.thorbenkuck.network.ObjectEncoder;
import com.github.thorbenkuck.network.RemoteMessage;
import com.github.thorbenkuck.network.connection.Connection;
import com.github.thorbenkuck.network.stream.EventStream;

import java.io.IOException;

public interface ServerContainer {

	static ServerContainer open(int port) throws IOException {
		return new NativeServerContainer(port);
	}

	EventStream<RemoteMessage> output();

	void accept();

	EventStream<Connection> ingoingConnections();

	ObjectEncoder getObjectEncoder();

	void setObjectEncoder(ObjectEncoder objectEncoder);

	ObjectDecoder getObjectDecoder();

	void setObjectDecoder(ObjectDecoder objectDecoder);

	void close() throws IOException;

	void closeSilently();
}
