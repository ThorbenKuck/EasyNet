package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.RemoteMessage;
import com.github.thorbenkuck.network.connection.ConnectionContext;
import com.github.thorbenkuck.network.encoding.ObjectDecoder;
import com.github.thorbenkuck.network.encoding.ObjectEncoder;
import com.github.thorbenkuck.network.stream.EventStream;

import java.io.IOException;

public interface ServerContainer extends AutoCloseable {

	static Builder builder() {
		return new ServerBuilder();
	}

	static ServerContainer open(int port) throws IOException {
		return open(port, new BlockingServerConnectionFactory());
	}

	static ServerContainer open(int port, ServerConnectionFactory connectionFactory) throws IOException {
		return new NativeServerContainer(port, connectionFactory);
	}

	EventStream<RemoteMessage> output();

	void accept();

	EventStream<ConnectionContext> ingoingConnections();

	ObjectEncoder getObjectEncoder();

	void setObjectEncoder(ObjectEncoder objectEncoder);

	ObjectDecoder getObjectDecoder();

	void setObjectDecoder(ObjectDecoder objectDecoder);

	void close() throws IOException;

	void closeSilently();

	int getPort();
}
