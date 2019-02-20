package com.github.thorbenkuck.network.connection;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

class NIOReadingSystem {

	private static NIOReadingSystem instance;
	private final NonBlockingReadingService readingService = new NonBlockingReadingService();
	private final Map<SocketChannel, NonBlockingConnection> mapping = new HashMap<>();

	private NIOReadingSystem() {
		Thread nioListenerThread = new Thread(readingService);
		nioListenerThread.setName("TCP Listener (NonBlocking)");
		nioListenerThread.start();
	}

	static NIOReadingSystem getInstance() {
		if (instance != null) {
			return instance;
		}

		synchronized (NIOReadingSystem.class) {
			// Check again to compensate race conditions
			if (instance != null) {
				return instance;
			}

			instance = new NIOReadingSystem();
			return instance;
		}
	}

	private NonBlockingConnection get(SocketChannel channel) {
		synchronized (mapping) {
			return mapping.get(channel);
		}
	}

	private void set(SocketChannel socketChannel, NonBlockingConnection connection) {
		synchronized (mapping) {
			mapping.put(socketChannel, connection);
		}
	}

	void unregister(SocketChannel socketChannel) {
		SelectionKey key = socketChannel.keyFor(readingService.readingSelector);

		if (key != null) {
			key.cancel();
		}
	}

	void registerForReading(SocketChannel socketChannel, NonBlockingConnection connection) {
		set(socketChannel, connection);
		readingService.append(socketChannel);
	}

	private final class NonBlockingReadingService implements Runnable {

		private final Selector readingSelector;
		private final LinkedBlockingQueue<SocketChannel> toRegister = new LinkedBlockingQueue<>();
		private boolean running = false;

		NonBlockingReadingService() {
			try {
				readingSelector = Selector.open();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		private void registerNew() {
			while (!toRegister.isEmpty()) {
				SocketChannel socketChannel = toRegister.poll();
				try {
					socketChannel.register(readingSelector, SelectionKey.OP_READ);
				} catch (ClosedChannelException ignored) {
					// Ignored a closed Channel, nothing
					// we can really do about this.
				}
			}
		}

		@Override
		public void run() {
			SelectionKey key;
			running = true;
			while (running) {
				try {
					readingSelector.select();

					Set<SelectionKey> selectedKeys = readingSelector.selectedKeys();
					Iterator<SelectionKey> iterator = selectedKeys.iterator();
					while (iterator.hasNext()) {
						key = iterator.next();
						iterator.remove();

						if (!key.isValid()) {
							continue;
						}

						System.out.println("New Event in ReadingSystem");

						if (key.isReadable()) {
							SocketChannel channel = (SocketChannel) key.channel();
							NonBlockingConnection nonBlockingConnection = get(channel);
							if (nonBlockingConnection == null) {
								System.err.println("No Connection found for " + channel);
							} else {
								byte[] data = nonBlockingConnection.readFromProtocol();
								nonBlockingConnection.received(data);
							}
						}
					}

					registerNew();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		void append(SocketChannel socketChannel) {
			try {
				toRegister.put(socketChannel);
				readingSelector.wakeup();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
