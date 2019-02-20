package com.github.thorbenkuck.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class DataInputConnection implements DataConnection {

	private final DataInputStream dataInput;
	private final DataOutputStream dataOutput;
	private final Socket socket;

	DataInputConnection(Socket socket) throws IOException {
		this.dataInput = new DataInputStream(socket.getInputStream());
		this.dataOutput = new DataOutputStream(socket.getOutputStream());
		this.socket = socket;
	}

	@Override
	public byte[] read(int i) throws IOException {
		byte[] buffer = new byte[i];
		dataInput.readFully(buffer);
		return buffer;
	}

	@Override
	public void write(byte[] data) throws IOException {
		dataOutput.write(data);
	}

	@Override
	public void flush() throws IOException {
		dataOutput.flush();
	}

	@Override
	public void close() throws IOException {
		socket.close();
		dataInput.close();
		dataOutput.close();
	}

	@Override
	public void closeSilent() {
		try {
			dataInput.close();
		} catch (IOException ignored) {
		}
		try {
			dataOutput.close();
		} catch (IOException ignored) {
		}
		try {
			socket.close();
		} catch (IOException ignored) {
		}
	}
}
