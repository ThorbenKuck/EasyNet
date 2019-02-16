package com.github.thorbenkuck.network;

import com.github.thorbenkuck.network.connection.Connection;

public final class RemoteMessage {

	private final Object dataObject;
	private final Connection connection;

	public RemoteMessage(Object dataObject, Connection connection) {
		this.dataObject = dataObject;
		this.connection = connection;
	}

	public Object getDataObject() {
		return dataObject;
	}

	public Connection getConnection() {
		return connection;
	}
}
