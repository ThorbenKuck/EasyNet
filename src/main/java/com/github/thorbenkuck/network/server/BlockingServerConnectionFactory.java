package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.PostConnectHandler;
import com.github.thorbenkuck.network.PostCreationHandler;
import com.github.thorbenkuck.network.connection.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class BlockingServerConnectionFactory implements ServerConnectionFactory {

	private ServerSocket serverSocket;
	private final Queue<PostConnectHandler> postConnectConsumers;
	private final Queue<PostCreationHandler> postCreationConsumers;

	public BlockingServerConnectionFactory() {
		this(Collections.emptyList(), Collections.emptyList());
	}

	public BlockingServerConnectionFactory(Collection<PostCreationHandler> postCreationConsumers, Collection<PostConnectHandler> postConnectConsumers) {
		this.postConnectConsumers = new LinkedList<>(postConnectConsumers);
		this.postCreationConsumers = new LinkedList<>(postCreationConsumers);
	}

	@Override
	public void listen(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		postCreationConsumers.forEach(consumer -> {
			try {
				consumer.accept(serverSocket);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});
	}

	@Override
	public Connection getNext() throws IOException {
		Socket socket = serverSocket.accept();
		postConnectConsumers.forEach(consumer -> {
			try {
				consumer.accept(socket);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});
		return Connection.wrap(socket);
	}

	@Override
	public void close() throws IOException {
		serverSocket.close();
	}
}
