package com.github.thorbenkuck.network.encoding;

import com.github.thorbenkuck.network.exceptions.FailedDecodingException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

public class JavaObjectDecoder implements ObjectDecoder {
	@Override
	public Object apply(byte[] bytes) throws FailedDecodingException {
		try (ByteArrayInputStream byteInput = new ByteArrayInputStream(bytes);
			 ObjectInput objectInput = new ObjectInputStream(byteInput)) {
			return objectInput.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new FailedDecodingException(e);
		}
	}

	@Override
	public String toString() {
		return "JavaObjectDecoder{}";
	}
}
