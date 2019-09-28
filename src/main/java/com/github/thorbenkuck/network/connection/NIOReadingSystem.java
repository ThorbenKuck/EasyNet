package com.github.thorbenkuck.network.connection;

import com.github.thorbenkuck.network.ThreadPools;
import com.github.thorbenkuck.network.WorkQueue;

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
import java.util.concurrent.atomic.AtomicBoolean;

public class NIOReadingSystem {

    private static NIOReadingSystem instance;
    private final NonBlockingReadingService readingService = new NonBlockingReadingService();
    private final Map<SocketChannel, NonBlockingConnection> mapping = new HashMap<>();
    private static AtomicBoolean implicitShutdown = new AtomicBoolean(true);

    public static void setImplicitShutdown(boolean b) {
        implicitShutdown.set(b);
    }

    public static void shutdownNow() {
        getInstance().readingService.shutdown();
    }

    private NIOReadingSystem() {
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

    private void remove(SocketChannel socketChannel) {
        synchronized (mapping) {
            mapping.remove(socketChannel);
        }
    }

    void unregister(SocketChannel socketChannel) {
        SelectionKey key = socketChannel.keyFor(readingService.readingSelector);

        if (key != null) {
            key.cancel();
        }

        remove(socketChannel);

        if (implicitShutdown.get() && mapping.size() == 0) {
            readingService.shutdown();
        }
    }

    void register(SocketChannel socketChannel, NonBlockingConnection connection) {
        set(socketChannel, connection);
        readingService.append(socketChannel);
        if (mapping.size() == 1) {
            ThreadPools.runDaemonThread(readingService, "TCP Listener (NonBlocking)");
        }
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
            if (running) {
                return;
            }
            SelectionKey key;
            running = true;
            while (running) {
                try {
                    readingSelector.select();

                    if (!running) {
                        break;
                    }

                    Set<SelectionKey> selectedKeys = readingSelector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    while (iterator.hasNext()) {
                        key = iterator.next();
                        iterator.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            NonBlockingConnection nonBlockingConnection = get(channel);
                            if (nonBlockingConnection == null) {
                                System.err.println("No Connection found for " + channel);
                            } else {
                                if (!channel.isConnected() || !channel.isOpen()) {
                                    nonBlockingConnection.closeSilently();
                                } else {
                                    try {
                                        byte[] data = nonBlockingConnection.readFromProtocol();
                                        if (data.length == 0) {
                                            nonBlockingConnection.closeSilently();
                                        } else {
                                            WorkQueue.append(() -> nonBlockingConnection.received(data));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        nonBlockingConnection.closeSilently();
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

        void shutdown() {
            running = false;
            readingSelector.wakeup();
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
