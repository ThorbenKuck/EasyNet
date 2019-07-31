package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.PostConnectHandler;
import com.github.thorbenkuck.network.PostCreationHandler;

public interface BlockingConnectionFactoryBuilder {

	BlockingConnectionFactoryBuilder afterConnect(PostConnectHandler socketConsumer);

	BlockingConnectionFactoryBuilder afterCreation(PostCreationHandler serverSocketConsumer);

	ServerConnectionFactory build();

}
