package com.github.thorbenkuck.network.client;

import com.github.thorbenkuck.network.connection.Connection;

import java.io.IOException;

public interface ClientConnectionFactory {

	Connection create(String address, int port) throws IOException;

}
