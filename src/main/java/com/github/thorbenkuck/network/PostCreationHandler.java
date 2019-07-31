package com.github.thorbenkuck.network;

import java.net.ServerSocket;

public interface PostCreationHandler {

	void accept(ServerSocket serverSocket) throws Exception;

}
