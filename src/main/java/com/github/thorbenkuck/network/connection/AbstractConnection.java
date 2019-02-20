package com.github.thorbenkuck.network.connection;

import com.github.thorbenkuck.network.DataConnection;
import com.github.thorbenkuck.network.Protocol;
import com.github.thorbenkuck.network.SizeFirstProtocol;
import com.github.thorbenkuck.network.stream.DataStream;
import com.github.thorbenkuck.network.stream.EventStream;
import com.github.thorbenkuck.network.stream.NativeEventStream;
import com.github.thorbenkuck.network.stream.WritableEventStream;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractConnection implements Connection {

	protected final WritableEventStream<byte[]> output = new NativeEventStream<>();
	protected final WritableEventStream<byte[]> input = new NativeEventStream<>();
	protected final WritableEventStream<String> systemInput = new NativeEventStream<>();
	protected final WritableEventStream<String> systemOutput = new NativeEventStream<>();
	protected final Object unknownExceptionHandlerLock = new Object();
	protected final Object protocolLock = new Object();
	private Consumer<Connection> onDisconnect;
	private BiConsumer<Connection, Throwable> unknownExceptionHandler = (connection, throwable) -> silentRawWrite("HTTP/1.1 401 UNAUTHORIZED".getBytes());
	private Protocol protocol = new SizeFirstProtocol();

	protected void pipeInputStreams() {
		input.subscribe(this::silentWrite);
		systemInput.subscribe(s -> silentWrite(("sys " + s).getBytes()));
	}

	protected abstract void write(byte[] bytes) throws IOException;

	protected void silentWrite(byte[] bytes) {
		try {
			write(bytes);
		} catch (IOException ignored) {
		}
	}

	protected abstract void rawWrite(byte[] bytes) throws IOException;

	protected void silentRawWrite(byte[] bytes) {
		try {
			rawWrite(bytes);
		} catch (IOException ignored) {
		}
	}

	protected void acceptError(Throwable throwable) {
		unknownExceptionHandler.accept(this, throwable);
	}

	protected void disconnectEvent() {
		if (onDisconnect != null) {
			onDisconnect.accept(this);
		}
	}

	@Override
	public Protocol getProtocol() {
		synchronized (protocolLock) {
			return protocol;
		}
	}

	@Override
	public void setProtocol(Protocol protocol) {
		if (protocol == null) {
			throw new IllegalArgumentException("Protocol must not be null!");
		}
		synchronized (protocolLock) {
			this.protocol = protocol;
		}
	}

	@Override
	public byte[] readFromProtocol() throws IOException {
		return getProtocol().readNext(getDataConnection());
	}

	@Override
	public void writeToProtocol(byte[] data) throws IOException {
		getProtocol().write(data, getDataConnection());
	}

	@Override
	public final EventStream<byte[]> output() {
		return output;
	}

	@Override
	public final DataStream<byte[]> input() {
		return input;
	}

	@Override
	public final EventStream<String> systemOutput() {
		return systemOutput;
	}

	@Override
	public final DataStream<String> systemInput() {
		return systemInput;
	}

	@Override
	public final Consumer<Connection> getOnDisconnect() {
		return onDisconnect;
	}

	@Override
	public final void setOnDisconnect(Consumer<Connection> onDisconnect) {
		this.onDisconnect = onDisconnect;
	}

	@Override
	public final void setUnknownExceptionHandler(BiConsumer<Connection, Throwable> handler) {
		synchronized (unknownExceptionHandlerLock) {
			unknownExceptionHandler = handler;
		}
	}

	public abstract DataConnection getDataConnection();

	@Override
	public void close() throws IOException {
		output.cut();
		input.cut();
	}

	@Override
	public final void pauseOutput(boolean b) {
		if (b) {
			output.pause();
		} else {
			output.unPause();
		}
	}
}
