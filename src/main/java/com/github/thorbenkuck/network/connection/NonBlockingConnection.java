package com.github.thorbenkuck.network.connection;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

class NonBlockingConnection extends AbstractConnection {

	private final SocketChannel socketChannel;
	private final DataConnection dataConnection;

	NonBlockingConnection(SocketChannel socketChannel) throws IOException {
		this.socketChannel = socketChannel;
		dataConnection = DataConnection.wrap(socketChannel);
		setProtocol(new SizeFirstProtocol());
		pipeInputStreams();
		output.subscribe(b -> System.out.println("[Receive]: " + b));
		systemOutput().subscribe(b -> System.out.println("[System, Receive]: " + b));
		systemInput.subscribe(b -> System.out.println("[System, Send]: " + b));
	}

	@Override
	public DataConnection getDataConnection() {
		return dataConnection;
	}

	@Override
	public void listen() {
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		NIOReadingSystem.getInstance().register(socketChannel, this);
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
		super.close();
		NIOReadingSystem.getInstance().unregister(socketChannel);
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
		try {
			super.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Unregistering from ReadingService ..");
		NIOReadingSystem.getInstance().unregister(socketChannel);
		System.out.println("Unregistering from WritingService ..");
		NIOWritingSystem.getInstance().unregister(socketChannel);
		try {
			socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		disconnectEvent();
	}

	void received(byte[] data) {
		if (data.length < 200) {
			String potential = new String(data);
			if (potential.toLowerCase().startsWith("sys")) {
				systemOutput.push(potential.substring(4));
			} else {
				output.push(data);
			}
		} else {
			output.push(data);
		}
	}
}
