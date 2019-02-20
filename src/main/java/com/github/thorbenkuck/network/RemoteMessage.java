package com.github.thorbenkuck.network;

import com.github.thorbenkuck.network.connection.ConnectionContext;

public final class RemoteMessage {

	private final Object dataObject;
	private final ConnectionContext connection;

	public RemoteMessage(Object dataObject, ConnectionContext connection) {
		this.dataObject = dataObject;
		this.connection = connection;
	}

	public Object getDataObject() {
		return dataObject;
	}

	public ConnectionContext getContext() {
		return connection;
	}
}
