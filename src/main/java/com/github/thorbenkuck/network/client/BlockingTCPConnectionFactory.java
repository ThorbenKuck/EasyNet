package com.github.thorbenkuck.network.client;

import com.github.thorbenkuck.network.connection.Connection;

import java.io.IOException;
import java.net.Socket;

public class BlockingTCPConnectionFactory implements ClientConnectionFactory {
	@Override
	public Connection create(String address, int port) throws IOException {
		Socket socket = new Socket(address, port);
		return Connection.wrap(socket);
	}
}
