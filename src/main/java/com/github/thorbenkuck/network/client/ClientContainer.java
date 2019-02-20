package com.github.thorbenkuck.network.client;

import com.github.thorbenkuck.network.ObjectDecoder;
import com.github.thorbenkuck.network.ObjectEncoder;
import com.github.thorbenkuck.network.RemoteMessage;
import com.github.thorbenkuck.network.connection.ConnectionContext;
import com.github.thorbenkuck.network.stream.DataStream;
import com.github.thorbenkuck.network.stream.EventStream;

import java.io.IOException;
import java.util.function.Consumer;

public interface ClientContainer {

	static ClientContainer open(String address, int port) throws IOException {
		return open(address, port, new BlockingTCPConnectionFactory());
	}

	static ClientContainer open(String address, int port, ClientConnectionFactory clientConnectionFactory) throws IOException {
		return new NativeClientContainer(address, port, clientConnectionFactory);
	}

	String getTargetAddress();

	int getTargetPort();

	void truncate(ClientContainer clientContainer);

	void append(ClientContainer clientContainer);

	EventStream<RemoteMessage> output();

	DataStream<Object> input();

	ObjectEncoder getObjectEncoder();

	void setObjectEncoder(ObjectEncoder objectEncoder);

	ObjectDecoder getObjectDecoder();

	void setObjectDecoder(ObjectDecoder objectDecoder);

	void sendRaw(byte[] data) throws IOException;

	void sendRaw(String message) throws IOException;

	void close() throws IOException;

	void closeSilently();

	void onDisconnect(Consumer<ConnectionContext> connectionConsumer);

	void listen(String id);

	void listen();

	String getIdentifier();

	void setIdentifier(String id);

	ClientContainer createSub() throws IOException;

	ClientContainer createSub(Consumer<ClientContainer> postCreationConsumer) throws IOException;
}
