package com.github.thorbenkuck.network.connection;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

class NonBlockingConnection extends AbstractConnection {

    private final SocketChannel socketChannel;
    private final DataConnection dataConnection;

    NonBlockingConnection(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        dataConnection = DataConnection.wrap(socketChannel);
        setProtocol(new SizeFirstProtocol());
        pipeInputStreams();
    }

    @Override
    public DataConnection getDataConnection() {
        return dataConnection;
    }

	@Override
	public void listen() {
		try {
			socketChannel.finishConnect();
			NIOReadingSystem.getInstance().register(socketChannel, this);
		} catch (IOException e) {
			acceptError(e);
		}
	}

	@Override
	public SocketAddress remoteAddress() {
		try {
			return socketChannel.getRemoteAddress();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public boolean isOpen() {
		return socketChannel.isOpen();
	}

	@Override
	public void close() throws IOException {
        NIOReadingSystem.getInstance().unregister(socketChannel);
        NIOWritingSystem.getInstance().unregister(socketChannel);
        super.close();
        socketChannel.close();
    }

	@Override
	public SocketAddress localAddress() {
		try {
			return socketChannel.getLocalAddress();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void closeSilently() {
        NIOReadingSystem.getInstance().unregister(socketChannel);
        NIOWritingSystem.getInstance().unregister(socketChannel);
        try {
            super.close();
        } catch (IOException ignored) {
        }
        try {
            socketChannel.close();
        } catch (IOException ignored) {
        }
    }
}
