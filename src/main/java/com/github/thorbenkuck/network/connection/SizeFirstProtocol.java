package com.github.thorbenkuck.network.connection;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SizeFirstProtocol implements Protocol {

	@Override
	public byte[] readNext(DataConnection dataConnection) throws IOException {
		byte[] rawSize;
		byte[] data;
		int size;

		rawSize = dataConnection.read(4);
		if (rawSize.length < 4) {
			return new byte[0];
		}
		size = toInt(rawSize);
		if (size > Runtime.getRuntime().freeMemory()) {
			throw new OutOfMemoryError("Not enough heap space for " + size + " bytes");
		}

		try {
			data = dataConnection.read(size);
		} catch (IllegalStateException e) {
			throw new IOException(e.getCause());
		}
		return data;
	}

	@Override
	public void write(byte[] data, DataConnection dataConnection) throws IOException {
		byte[] buffer = ByteBuffer.allocate(4 + data.length)
				.putInt(data.length)
				.put(data)
				.array();

		dataConnection.write(buffer);
		dataConnection.flush();
	}
}
