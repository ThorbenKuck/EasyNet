package com.github.thorbenkuck.network.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class SocketChannelDataConnection implements DataConnection {

	private final SocketChannel channel;

	SocketChannelDataConnection(SocketChannel channel) {
		this.channel = channel;
	}

	@Override
	public byte[] read(int i) throws IOException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(i);
		channel.read(byteBuffer);
		byteBuffer.flip();
		return byteBuffer.array();
	}

	@Override
	public void write(byte[] data) throws IOException {
		ByteBuffer wrap = ByteBuffer.wrap(data);
		channel.write(wrap);
	}

	@Override
	public void flush() throws IOException {
		// This is not needed nor possible
	}

	@Override
	public void close() throws IOException {
		channel.shutdownInput()
				.shutdownOutput()
				.close();
	}

	@Override
	public void closeSilent() {
		try {
			channel.shutdownInput();
		} catch (IOException ignored) {
		}
		try {
			channel.shutdownOutput();
		} catch (IOException ignored) {
		}
		try {
			channel.close();
		} catch (IOException ignored) {
		}
	}
}
