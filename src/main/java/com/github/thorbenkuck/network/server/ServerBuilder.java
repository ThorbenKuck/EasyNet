package com.github.thorbenkuck.network.server;

import java.io.IOException;

public class ServerBuilder implements Builder {

	private ServerConnectionFactory connectionFactory = new BlockingServerConnectionFactory();

	@Override
	public Builder blocking() {
		withCustomConnectionFactory(new BlockingServerConnectionFactory());

		return this;
	}

	@Override
	public Builder nonBlocking() {
		withCustomConnectionFactory(new NonBlockingServerConnectionFactory());

		return this;
	}

	@Override
	public ServerContainer build(int port) throws IOException {
		return new NativeServerContainer(port, connectionFactory);
	}

	@Override
	public Builder withCustomConnectionFactory(ServerConnectionFactory connectionFactory) {
		if (connectionFactory == null) {
			throw new IllegalArgumentException("Null is an invalid ServerConnectionFactory!");
		}
		this.connectionFactory = connectionFactory;

		return this;
	}
}
