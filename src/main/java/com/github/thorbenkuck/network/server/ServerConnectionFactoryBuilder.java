package com.github.thorbenkuck.network.server;

public class ServerConnectionFactoryBuilder implements ConnectionFactoryBuilder {

	@Override
	public NonBlockingConnectionFactoryBuilder nonBlocking() {
		return new NonBlockingServerConnectionFactoryBuilder();
	}

	@Override
	public BlockingConnectionFactoryBuilder blocking() {
		return new BlockingServerConnectionFactoryBuilder();
	}
}
