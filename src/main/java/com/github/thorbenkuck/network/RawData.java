package com.github.thorbenkuck.network;

public class RawData {

	private final byte[] data;

	public RawData(byte[] data) {
		this.data = data;
	}

	public byte[] getBytes() {
		return data;
	}
}
