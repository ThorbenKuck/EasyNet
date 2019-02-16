package com.github.thorbenkuck.network;

import java.io.*;

public class JavaObjectEncoder implements ObjectEncoder {
	@Override
	public byte[] apply(Object o) {
		try (ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			 ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream)) {
			objectOutputStream.writeObject(o);
			objectOutputStream.flush();
			return byteOutputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}
}
