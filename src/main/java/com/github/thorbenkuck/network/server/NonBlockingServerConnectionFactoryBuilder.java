package com.github.thorbenkuck.network.server;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NonBlockingServerConnectionFactoryBuilder implements NonBlockingConnectionFactoryBuilder {

	private final List<Consumer<ServerSocketChannel>> creationConsumers = new ArrayList<>();
	private final List<Consumer<SocketChannel>> connectConsumers = new ArrayList<>();

	@Override
	public void afterCreation(Consumer<ServerSocketChannel> socketChannelConsumer) {
		creationConsumers.add(socketChannelConsumer);
	}

	@Override
	public void afterConnect(Consumer<SocketChannel> socketChannelConsumer) {
		connectConsumers.add(socketChannelConsumer);
	}

	@Override
	public ServerConnectionFactory build() {
		NonBlockingServerConnectionFactory nonBlockingServerConnectionFactory = new NonBlockingServerConnectionFactory(connectConsumers, creationConsumers);
		return nonBlockingServerConnectionFactory;
	}
}
