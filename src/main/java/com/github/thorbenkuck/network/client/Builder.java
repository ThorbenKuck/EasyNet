package com.github.thorbenkuck.network.client;

import java.io.IOException;

public interface Builder {

	Builder blocking();

	Builder nonBlocking();

	ClientContainer build(String address, int port) throws IOException;

	Builder withCustomConnectionFactory(ClientConnectionFactory clientConnectionFactory);

}
