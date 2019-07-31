package com.github.thorbenkuck.network;

import java.net.Socket;

public interface PostConnectHandler {

	void accept(Socket socket) throws Exception;

}
