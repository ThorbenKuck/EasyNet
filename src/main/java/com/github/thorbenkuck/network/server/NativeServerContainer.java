package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.RemoteMessage;
import com.github.thorbenkuck.network.connection.Connection;
import com.github.thorbenkuck.network.connection.ConnectionContext;
import com.github.thorbenkuck.network.encoding.JavaObjectDecoder;
import com.github.thorbenkuck.network.encoding.JavaObjectEncoder;
import com.github.thorbenkuck.network.encoding.ObjectDecoder;
import com.github.thorbenkuck.network.encoding.ObjectEncoder;
import com.github.thorbenkuck.network.exceptions.FailedDecodingException;
import com.github.thorbenkuck.network.exceptions.FailedEncodingException;
import com.github.thorbenkuck.network.stream.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

class NativeServerContainer implements ServerContainer {

	private final WritableEventStream<ConnectionContext> connected = new NativeEventStream<>();
	private final WritableEventStream<RemoteMessage> outStream = new NativeEventStream<>();
	private final int port;
	private final ServerConnectionFactory serverConnectionFactory;
	private final AtomicBoolean accepting = new AtomicBoolean(false);
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private final Map<String, ConnectionContext> connectionMap = new HashMap<>();
	private ObjectEncoder objectEncoder = new JavaObjectEncoder();
	private ObjectDecoder objectDecoder = new JavaObjectDecoder();

	NativeServerContainer(int port, ServerConnectionFactory serverConnectionFactory) throws IOException {
		this.port = port;
		this.serverConnectionFactory = serverConnectionFactory;
		serverConnectionFactory.listen(port);
		connected.subscribe(this::newConnection);
	}

	private byte[] convert(Object object) {
		try {
			return objectEncoder.apply(object);
		} catch (FailedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	private void newConnection(ConnectionContext context) {
		context.outputStream().subscribe(rawData -> handleReceive(rawData, context));
	}

	private void handleReceive(byte[] rawData, ConnectionContext context) {
		try {
			Object o = objectDecoder.apply(rawData);
			outStream.push(new RemoteMessage(o, context));
		} catch (FailedDecodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void handshake(Connection connection) {
		final CompletableFuture<String> future = new CompletableFuture<>();
		final String id = UUID.randomUUID().toString();
		final Subscription temporary = connection.systemOutput().subscribe(new SetupSubscriber(id, connection, future));
		connection.pauseOutput(true);
		connection.listen();

		connection.systemInput().push("id " + id);

		try {
			final String result = future.get();
			temporary.cancel();
			if (result.toLowerCase().endsWith("reject")) {
				System.err.println("Handshake failed!\n### Protocol-Start ###\n" + result + "### Protocol-End  ###");
				connection.closeSilently();
			} else {
				final ConnectionContext connectionContext = ConnectionContext.map(connection, () -> result, this::convert);
				connectionMap.put(result, connectionContext);
				connected.push(connectionContext);
				connection.pauseOutput(false);
			}
		} catch (InterruptedException ignored) {
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			temporary.cancel();
		}
	}

	@Override
	public EventStream<RemoteMessage> output() {
		return outStream;
	}

	@Override
	public void accept() {
		accepting.set(true);
		while (accepting.get()) {
			try {
				Connection connection = serverConnectionFactory.getNext();
				executorService.submit(() -> handshake(connection));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public EventStream<ConnectionContext> ingoingConnections() {
		return connected;
	}

	@Override
	public ObjectEncoder getObjectEncoder() {
		return objectEncoder;
	}

	@Override
	public void setObjectEncoder(ObjectEncoder objectEncoder) {
		this.objectEncoder = objectEncoder;
	}

	@Override
	public ObjectDecoder getObjectDecoder() {
		return objectDecoder;
	}

	@Override
	public void setObjectDecoder(ObjectDecoder objectDecoder) {
		this.objectDecoder = objectDecoder;
	}

	@Override
	public void close() throws IOException {
		outStream.close();
		serverConnectionFactory.close();
	}

	@Override
	public void closeSilently() {
		outStream.close();

		try {
			serverConnectionFactory.close();
		} catch (IOException ignored) {
		}
	}

	@Override
	public int getPort() {
		return port;
	}

	private final class SetupSubscriber implements Subscriber<String> {

		private final Connection connection;
		private final CompletableFuture<String> future;
		private final StringBuilder stringBuilder = new StringBuilder();
		private String id;

		private SetupSubscriber(String id, Connection connection, CompletableFuture<String> future) {
			this.id = id;
			this.connection = connection;
			this.future = future;
		}

		private void exit(String message, String exitVal) {
			connection.systemInput().push(message);
			future.complete(exitVal);
		}

		@Override
		public void accept(String message) throws Exception {
			stringBuilder.append(message).append(System.lineSeparator());
			if (message.toLowerCase().equals("ok")) {
				exit("ok", id);
			} else if (message.toLowerCase().startsWith("request")) {
				String requested = message.substring(8);
				if (requested.equals(id)) {
					connection.systemInput().push("id " + id);
				} else {
					ConnectionContext targetContext = connectionMap.get(requested);
					if (targetContext == null) {
						exit("reject", stringBuilder.append("no target found").append(System.lineSeparator()).append("reject").toString());
					} else {
						CompletableFuture<String> future = new CompletableFuture<>();
						Subscription subscription = targetContext.systemOutput().subscribe(future::complete);

						targetContext.systemInput().push("known " + id);
						stringBuilder.append("Requesting ").append(id).append(" for knowledge .. ");
						String result = future.get();
						subscription.cancel();

						if (result.toLowerCase().equals("ok")) {
							stringBuilder.append("[OK]").append(System.lineSeparator());
							id = requested;
							connection.systemInput().push("id " + requested);
						} else {
							stringBuilder.append("[ERR]").append(System.lineSeparator());
							System.err.println("!WARN! Target( " + targetContext.remoteAddress() + ") does NOT know origin (" + connection.remoteAddress() + ")! Initializing disconnect");
							exit("reject", stringBuilder.append("reject").toString());
						}
					}
				}
			} else {
				System.err.println("Unknown system message " + message);
				exit("reject", stringBuilder.append("unknown request ").append(message).append(System.lineSeparator()).append("reject").toString());
			}
		}
	}
}
