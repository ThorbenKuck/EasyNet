package com.github.thorbenkuck.network;

import com.github.thorbenkuck.network.connection.ConnectionContext;

public final class RemoteMessage {

	private final Object data;
	private final ConnectionContext context;

	public RemoteMessage(Object data, ConnectionContext connection) {
		this.data = data;
		this.context = connection;
	}

	public Object data() {
		return data;
	}

	public ConnectionContext context() {
		return context;
	}

	@Override
	public String toString() {
		return "RemoteMessage{" +
				"data=" + data +
				", context=" + context +
				'}';
	}
}
