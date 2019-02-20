package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.connection.Connection;

import java.io.IOException;

public interface ServerConnectionFactory {

	void listen(int port) throws IOException;

	Connection getNext() throws IOException;

	void close() throws IOException;
}
