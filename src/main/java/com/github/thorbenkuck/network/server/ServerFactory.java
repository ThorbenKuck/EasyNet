package com.github.thorbenkuck.network.server;

public interface ServerFactory {

	static ServerFactory create() {
		return new NativeServerFactory();
	}

	Server construct();

}
