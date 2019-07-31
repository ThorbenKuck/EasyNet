package com.github.thorbenkuck.network.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class NIOWritingSystem {

	private static NIOWritingSystem instance;
	private final WritingService writingService = new WritingService();
	private final Map<SocketChannel, ByteBuffer> mapping = new HashMap<>();
	private static AtomicBoolean implicitShutdown = new AtomicBoolean(true);

	public static void setImplicitShutdown(boolean b) {
		implicitShutdown.set(b);
	}

	public static void shutdownNow() {
		getInstance().writingService.shutdown();
	}

	private NIOWritingSystem() {
	}

	static NIOWritingSystem getInstance() {
		if (instance != null) {
			return instance;
		}

		synchronized (NIOWritingSystem.class) {
			// Check again to compensate race conditions
			if (instance != null) {
				return instance;
			}

			instance = new NIOWritingSystem();
			return instance;
		}
	}

	void unregister(SocketChannel socketChannel) {
		SelectionKey key = socketChannel.keyFor(writingService.selector);

		if (key != null) {
			key.cancel();
		}

		if (implicitShutdown.get() && mapping.size() == 0) {
			writingService.shutdown();
		}
	}

	void put(SocketChannel channel, ByteBuffer byteBuffer) {
		mapping.put(channel, byteBuffer);
		if (!writingService.running) {
			Thread nioListenerThread = new Thread(writingService);
			nioListenerThread.setName("TCP Writer (NonBlocking)");
			nioListenerThread.start();
		}
		writingService.append(channel);
	}

	private class WritingService implements Runnable {

		private final Selector selector;
		private final LinkedBlockingQueue<SocketChannel> toRegister = new LinkedBlockingQueue<>();
		private boolean running = false;

		WritingService() {
			try {
				selector = Selector.open();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		private void registerNew() {
			while (!toRegister.isEmpty()) {
				SocketChannel socketChannel = toRegister.poll();
				try {
					socketChannel.register(selector, SelectionKey.OP_WRITE);
				} catch (ClosedChannelException ignored) {
					// Ignored a closed Channel, nothing
					// we can really do about this.
				}
			}
		}

		void shutdown() {
			running = false;
			selector.wakeup();
		}

		@Override
		public void run() {
			SelectionKey key;
			running = true;
			while (running) {
				try {
					selector.select();

					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iterator = selectedKeys.iterator();
					while (iterator.hasNext()) {
						key = iterator.next();
						iterator.remove();

						if (!key.isValid()) {
							continue;
						}

						if (key.isWritable()) {
							SocketChannel channel = (SocketChannel) key.channel();
							ByteBuffer buffer = mapping.remove(channel);
							if (buffer == null) {
								System.err.println("No data found for " + channel);
								channel.keyFor(selector).cancel();
							} else {
								int read = channel.write(buffer);
								if (read != 0) {
									if (!buffer.hasRemaining()) {
										channel.keyFor(selector).cancel();
									} else {
										buffer.compact();
										mapping.put(channel, buffer);
									}
								}
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
				selector.wakeup();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
