package com.github.thorbenkuck.network.encoding;

import com.github.thorbenkuck.network.exceptions.FailedEncodingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JavaObjectEncoder implements ObjectEncoder {

	@Override
	public byte[] apply(Object o) throws FailedEncodingException {
		try (ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			 ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream)) {
			objectOutputStream.writeObject(o);
			objectOutputStream.flush();
			return byteOutputStream.toByteArray();
		} catch (IOException e) {
			throw new FailedEncodingException(e);
		}
	}

	@Override
	public String toString() {
		return "JavaObjectEncoder{}";
	}
}
