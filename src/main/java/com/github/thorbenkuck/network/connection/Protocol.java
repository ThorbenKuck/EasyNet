package com.github.thorbenkuck.network.connection;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Protocol {

	default int toInt(byte[] data) {
		if (data.length != 4) {
			throw new IllegalArgumentException("Integer requires 4 byte");
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		return byteBuffer.getInt();
	}

	byte[] readNext(DataConnection dataConnection) throws IOException;

	void write(byte[] data, DataConnection dataConnection) throws IOException;

}
