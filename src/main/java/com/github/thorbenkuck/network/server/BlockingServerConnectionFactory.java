package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.connection.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingServerConnectionFactory implements ServerConnectionFactory {

	private ServerSocket serverSocket;

	@Override
	public void listen(int port) throws IOException {
		serverSocket = new ServerSocket(port);
	}

	@Override
	public Connection getNext() throws IOException {
		Socket socket = serverSocket.accept();
		return Connection.wrap(socket);
	}

	@Override
	public void close() throws IOException {
		serverSocket.close();
	}
}
