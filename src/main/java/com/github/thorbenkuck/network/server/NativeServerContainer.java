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
import com.github.thorbenkuck.network.stream.EventStream;
import com.github.thorbenkuck.network.stream.SimpleEventStream;
import com.github.thorbenkuck.network.stream.Subscription;
import com.github.thorbenkuck.network.stream.WritableEventStream;
import com.github.thorbenkuck.network.utils.PropertyUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

class NativeServerContainer implements ServerContainer {

	private final WritableEventStream<ConnectionContext> connected = new SimpleEventStream<>();
	private final WritableEventStream<RemoteMessage> outStream = new SimpleEventStream<>();
	private final int port;
    private final boolean exitOnError = PropertyUtils.exitOnError();
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
		final String id = UUID.randomUUID().toString();
		final ServerHandshakeSubscriber serverHandshakeSubscriber = new ServerHandshakeSubscriber(id, connection, connectionMap::get);
		final Subscription temporary = connection.systemOutput().subscribe(serverHandshakeSubscriber);
		final Future<String> future = serverHandshakeSubscriber.future();

		connection.pauseOutput();
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
				connection.unpauseOutput();
			}
		} catch (InterruptedException | ExecutionException e) {
			connected.publishError(e);
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
				connected.publishError(e);
                if (exitOnError) {
                    closeSilently();
                }
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
        accepting.set(false);
		outStream.close();
		serverConnectionFactory.close();
	}

	@Override
	public void closeSilently() {
        accepting.set(false);
		outStream.close();

		try {
			serverConnectionFactory.close();
		} catch (IOException e) {
			connected.publishError(e);
		}
	}

	@Override
	public int getPort() {
		return port;
	}
}
