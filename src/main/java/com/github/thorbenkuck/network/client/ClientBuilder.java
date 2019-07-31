package com.github.thorbenkuck.network.client;

import java.io.IOException;

public class ClientBuilder implements Builder {

	private ClientConnectionFactory clientConnectionFactory;

	@Override
	public Builder blocking() {
		withCustomConnectionFactory(new BlockingTCPConnectionFactory());

		return this;
	}

	@Override
	public Builder nonBlocking() {
		withCustomConnectionFactory(new NonBlockingTCPConnectionFactory());

		return this;
	}

	@Override
	public ClientContainer build(String address, int port) throws IOException {
		return new NativeClientContainer(address, port, clientConnectionFactory);
	}

	@Override
	public Builder withCustomConnectionFactory(ClientConnectionFactory clientConnectionFactory) {
		if (clientConnectionFactory == null) {
			throw new IllegalArgumentException("Null is an invalid ServerConnectionFactory!");
		}
		this.clientConnectionFactory = clientConnectionFactory;

		return this;
	}
}
