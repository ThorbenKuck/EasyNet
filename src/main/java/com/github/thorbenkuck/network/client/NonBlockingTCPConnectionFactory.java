package com.github.thorbenkuck.network.client;

import com.github.thorbenkuck.network.connection.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class NonBlockingTCPConnectionFactory implements ClientConnectionFactory {
	@Override
	public Connection create(String address, int port) throws IOException {
		SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(address, port));
		socketChannel.configureBlocking(false);
		return Connection.wrap(socketChannel);
	}
}
