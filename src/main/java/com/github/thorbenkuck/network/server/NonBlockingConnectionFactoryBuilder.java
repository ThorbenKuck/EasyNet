package com.github.thorbenkuck.network.server;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

public interface NonBlockingConnectionFactoryBuilder {

	void afterCreation(Consumer<ServerSocketChannel> socketChannelConsumer);

	void afterConnect(Consumer<SocketChannel> socketChannelConsumer);

	ServerConnectionFactory build();

}
