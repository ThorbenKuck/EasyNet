package com.github.thorbenkuck.network.server;

public interface ConnectionFactoryBuilder {

	NonBlockingConnectionFactoryBuilder nonBlocking();

	BlockingConnectionFactoryBuilder blocking();
}
