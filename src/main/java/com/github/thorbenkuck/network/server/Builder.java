package com.github.thorbenkuck.network.server;

import java.io.IOException;

public interface Builder {

	Builder blocking();

	Builder nonBlocking();

	ServerContainer build(int port) throws IOException;

	Builder withCustomConnectionFactory(ServerConnectionFactory serverConnectionFactory);
}
