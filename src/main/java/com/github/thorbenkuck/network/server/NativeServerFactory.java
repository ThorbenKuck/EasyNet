package com.github.thorbenkuck.network.server;

public class NativeServerFactory implements ServerFactory {

	private int port;

	@Override
	public Server construct() {
		return new NativeServer(port);
	}
}
