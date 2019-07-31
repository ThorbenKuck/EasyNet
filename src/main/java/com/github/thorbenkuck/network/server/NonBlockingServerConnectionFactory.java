package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.connection.Connection;
import jdk.jfr.Experimental;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Experimental
public class NonBlockingServerConnectionFactory implements ServerConnectionFactory {

	private final LinkedBlockingQueue<SocketChannel> connected = new LinkedBlockingQueue<>();
	private final List<Consumer<SocketChannel>> connectConsumers;
	private final List<Consumer<ServerSocketChannel>> creationConsumers;
	private ServerSocketChannel serverSocketChannel;
	private ConnectionListener connectionListener;

	public NonBlockingServerConnectionFactory() {
		this(new ArrayList<>(), new ArrayList<>());
	}

	public NonBlockingServerConnectionFactory(List<Consumer<SocketChannel>> connectConsumers, List<Consumer<ServerSocketChannel>> creationConsumers) {
		this.connectConsumers = connectConsumers;
		this.creationConsumers = creationConsumers;
	}

	@Override
	public void listen(int port) throws IOException {
		if (serverSocketChannel != null) {
			throw new IllegalStateException("Already listening");
		}
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.bind(new InetSocketAddress(port));
		creationConsumers.forEach(consumer -> consumer.accept(serverSocketChannel));
		connectionListener = new ConnectionListener(serverSocketChannel);
		Thread thread = new Thread(connectionListener);
		thread.setName("TCP Connection Listener");
		thread.start();
	}

	@Override
	public Connection getNext() throws IOException {
		if (serverSocketChannel == null) {
			throw new IllegalStateException("Not listening");
		}
		try {
			SocketChannel socketChannel = connected.take();
			connectConsumers.forEach(consumer -> consumer.accept(socketChannel));
			return Connection.wrap(socketChannel);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		if (serverSocketChannel == null) {
			throw new IllegalStateException("Not listening");
		}
		serverSocketChannel.keyFor(connectionListener.selector).cancel();
		serverSocketChannel.close();
	}

	private final class ConnectionListener implements Runnable {

		private final Selector selector;
		private boolean running = false;

		private ConnectionListener(ServerSocketChannel serverSocketChannel) throws IOException {
			selector = Selector.open();
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		}

		@Override
		public void run() {
			running = true;
			SelectionKey key;
			while (running) {
				try {
					if (selector.select() != 0) {
						Set<SelectionKey> selectedKeys = selector.selectedKeys();
						Iterator<SelectionKey> iterator = selectedKeys.iterator();

						while (iterator.hasNext()) {
							key = iterator.next();
							iterator.remove();

							if (!key.isValid()) {
								continue;
							}

							if (key.isAcceptable()) {
								SocketChannel sc = serverSocketChannel.accept();
								sc.configureBlocking(false);
								try {
									connected.put(sc);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
