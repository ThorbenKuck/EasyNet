package com.github.thorbenkuck.network.connection;

import com.github.thorbenkuck.network.stream.DataStream;
import com.github.thorbenkuck.network.stream.EventStream;

import java.net.SocketAddress;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ConnectionContext {

	static ConnectionContext map(Connection connection, Supplier<String> supplier, Function<Object, byte[]> convert) {
		return new NativeConnectionContext(connection, supplier, convert);
	}

	void send(Object o);

	DataStream<byte[]> inputStream();

	EventStream<byte[]> outputStream();

	DataStream<String> systemInput();

	EventStream<String> systemOutput();

	String getIdentifier();

	SocketAddress remoteAddress();

	Connection connection();

	void onDisconnect(Consumer<ConnectionContext> consumer);

	void onDisconnect(Runnable runnable);
}
