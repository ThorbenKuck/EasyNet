package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.PostConnectHandler;
import com.github.thorbenkuck.network.PostCreationHandler;

import java.util.ArrayList;
import java.util.List;

public class BlockingServerConnectionFactoryBuilder implements BlockingConnectionFactoryBuilder {

	private final List<PostConnectHandler> socketConsumers = new ArrayList<>();
	private final List<PostCreationHandler> serverSocketConsumers = new ArrayList<>();

	@Override
	public BlockingConnectionFactoryBuilder afterConnect(PostConnectHandler socketChannelConsumer) {
		socketConsumers.add(socketChannelConsumer);

		return this;
	}

	@Override
	public BlockingConnectionFactoryBuilder afterCreation(PostCreationHandler serverSocketConsumer) {
		serverSocketConsumers.add(serverSocketConsumer);

		return this;
	}

	@Override
	public ServerConnectionFactory build() {
		return new BlockingServerConnectionFactory(serverSocketConsumers, socketConsumers);
	}
}
