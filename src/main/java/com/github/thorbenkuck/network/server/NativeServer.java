package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.CommunicationRegistration;
import com.github.thorbenkuck.network.connection.Connection;

import java.io.IOException;

class NativeServer implements Server {

	private final CommunicationRegistration communicationRegistration = new CommunicationRegistration();
	private int port;
	private ServerContainer serverContainer;

	NativeServer(int port) {
		this.port = port;
	}

	@Override
	public void launch() throws IOException {
		serverContainer = ServerContainer.open(port);
		serverContainer.output().subscribe(remoteMessage -> communicationRegistration.trigger(remoteMessage.getDataObject(), remoteMessage.getConnection(), remoteMessage.getConnection().session()));
	}

	@Override
	public void shutdown() throws IOException {
		serverContainer.close();
	}

	@Override
	public void shutdownSilently() throws IOException {
		serverContainer.close();
	}

	@Override
	public CommunicationRegistration communications() {
		return communicationRegistration;
	}

	@Override
	public void onConnect(Connection connection) {

	}

	@Override
	public void onDisconnect(Connection connection) {

	}
}
