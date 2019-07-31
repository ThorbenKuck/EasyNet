package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.connection.Connection;

import java.io.IOException;

public interface ServerConnectionFactory {

	static ServerConnectionFactory blocking() {
		return new BlockingServerConnectionFactory();
	}

	static ServerConnectionFactory nonBlocking() {
		return new NonBlockingServerConnectionFactory();
	}

	static ServerConnectionFactoryBuilder builder() {
		return new ServerConnectionFactoryBuilder();
	}

	void listen(int port) throws IOException;

	Connection getNext() throws IOException;

	void close() throws IOException;
}
