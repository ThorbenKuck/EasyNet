package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.CommunicationRegistration;
import com.github.thorbenkuck.network.connection.Connection;

import java.io.IOException;

public interface Server {

	void launch() throws IOException;

	void shutdown() throws IOException;

	void shutdownSilently() throws IOException;

	CommunicationRegistration communications();

	void onConnect(Connection connection);

	void onDisconnect(Connection connection);

}
