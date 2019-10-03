package com.github.thorbenkuck.network.client;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

class NativeClientContainer implements ClientContainer {

	private final Connection connection;
	private final ConnectionContext connectionContext;
	private final ManagedEventStream<RemoteMessage> outStream = ManagedEventStream.sequential();
	private final ManagedEventStream<Object> inStream = ManagedEventStream.sequential();
	private final List<ClientContainer> connected = new ArrayList<>();
	private final String address;
	private final int port;
	private final ClientConnectionFactory factory;
	private ObjectEncoder objectEncoder = new JavaObjectEncoder();
	private ObjectDecoder objectDecoder = new JavaObjectDecoder();
	private String id;

	NativeClientContainer(String address, int port, ClientConnectionFactory factory) throws IOException {
		this.factory = factory;
		connection = factory.create(address, port);
		connectionContext = ConnectionContext.map(connection, this::getIdentifier, this::convert);
		connectionContext.onDisconnect(outStream::close);
		connectionContext.onDisconnect(inStream::close);
		inStream.subscribe(this::send);
		this.address = address;
		this.port = port;
	}

	private byte[] convert(Object o) {
		try {
			return objectEncoder.apply(o);
		} catch (FailedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	private void handleReceive(byte[] rawData) {
		try {
			Object o = objectDecoder.apply(rawData);
			outStream.push(new RemoteMessage(o, connectionContext));
		} catch (FailedDecodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private boolean doIKnow(String id) {
		return connected.stream()
				.map(ClientContainer::getIdentifier)
				.anyMatch(id::equals);
	}

	private void handleSystemRequest(String s) {
		if (s.toLowerCase().startsWith("known")) {
			String id = s.substring(6);
			if (doIKnow(id)) {
				connection.systemInput().push("ok");
			} else {
				connection.systemInput().push("rejected");
			}
		}
	}

	private void send(Object o) {
		connection.input().push(convert(o));
	}

	@Override
	public String getTargetAddress() {
		return address;
	}

	@Override
	public int getTargetPort() {
		return port;
	}

	@Override
	public void truncate(ClientContainer clientContainer) {
		NativeClientContainer that = (NativeClientContainer) clientContainer;
		if (this == that) {
			return;
		}
		that.connected.remove(this);
		this.connected.remove(that);
	}

	@Override
	public void append(ClientContainer clientContainer) {
		NativeClientContainer that = (NativeClientContainer) clientContainer;
		if (this == that) {
			return;
		}
		that.connected.add(this);
		this.connected.add(that);

		that.onDisconnect(connect -> truncate(that));
		this.onDisconnect(connect -> that.closeSilently());

		that.setIdentifier(getIdentifier());
		that.listen();
	}

	@Override
	public EventStream<RemoteMessage> output() {
		return outStream;
	}

	@Override
	public DataStream<Object> input() {
		return inStream;
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
	public void sendRaw(byte[] data) {
		connection.input().push(data);
	}

	@Override
	public void sendRaw(String message) {
		sendRaw(message.getBytes());
	}

	@Override
	public void close() throws IOException {
		connection.close();
	}

	@Override
	public void closeSilently() {
		connection.closeSilently();
	}

	@Override
	public void onDisconnect(Consumer<ConnectionContext> connectionConsumer) {
		connectionContext.onDisconnect(connectionConsumer);
	}

	@Override
	public void listen(String id) {
		setIdentifier(id);
		listen();
	}

	@Override
	public void listen() {
		CompletableFuture<String> future = new CompletableFuture<>();
		Subscription temporary = connection.systemOutput().subscribe(new ClientHandshakeSubscriber(future));
		connection.pauseOutput();

		connection.listen();

		try {
			String result = future.get();
			if (!result.toLowerCase().equals("ok")) {
				closeSilently();
				throw new IllegalAccessError("Handshake failed!\n### Protocol-Start ###\n" + result + "\n### Protocol-End  ###");
			}
			connection.systemOutput().subscribe(this::handleSystemRequest);
			connection.output().subscribe(this::handleReceive);
			connection.unpauseOutput();
		} catch (InterruptedException ignored) {
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			temporary.cancel();
		}
	}

	@Override
	public String toString() {
		return "NativeClientContainer{" + "connection=" + connection +
				", objectEncoder=" + objectEncoder +
				", objectDecoder=" + objectDecoder +
				", id='" + id + '\'' +
				'}';
	}

	@Override
	public String getIdentifier() {
		return id;
	}

	@Override
	public void setIdentifier(String id) {
		if (this.id != null) {
			throw new IllegalStateException("Connection identifier cannot be changed once set!");
		}

		this.id = id;
	}

	@Override
	public ClientContainer createSub() throws IOException {
		ClientContainer sub = ClientContainer.open(getTargetAddress(), getTargetPort());
		append(sub);

		return sub;
	}

	/**
	 * Creates a new ClientContainer, connected to the same {@link #getTargetAddress() targetAddress} and {@link #getTargetPort() targetPort},
	 * holding the same {@link ClientConnectionFactory connectionFactory}
	 * <p>
	 * This method is structurally the same to {@link #createSub()}. It differs in the fact, that before telling the
	 * new sub-container to listen, the postCreationConsumer is called. This can be used, if you need to register a
	 * subscriber to the {@link #output() DataStream} before you listen for incoming objects.
	 *
	 * @param postCreationConsumer the consumer, that will be triggered, before it starts listening
	 * @return an already listening ClientContainer at the same address as this one
	 * @throws IOException if anything goes wrong wile creating the Container
	 */
	@Override
	public ClientContainer createSub(Consumer<ClientContainer> postCreationConsumer) throws IOException {
		ClientContainer sub = ClientContainer.open(getTargetAddress(), getTargetPort(), factory);
		postCreationConsumer.accept(sub);
		append(sub);

		return sub;
	}

	private final class ClientHandshakeSubscriber implements Subscriber<String> {

		private final CompletableFuture<String> future;
		private final StringBuilder builder = new StringBuilder();
		private String idBuffer;

		private ClientHandshakeSubscriber(CompletableFuture<String> future) {
			this.future = future;
		}

		@Override
		public void accept(String message) {
			builder.append(message).append(System.lineSeparator());
			if (message.toLowerCase().startsWith("id")) {
				String identifier = message.substring(3);
				if (id != null && !id.equals(identifier)) {
					if (identifier.equals(idBuffer)) {
						connection.systemInput().push("ok");
						id = idBuffer;
					} else {
						builder.append("Requesting to use ").append(id).append(System.lineSeparator());
						idBuffer = id;
						id = identifier;
						connection.systemInput().push("request " + idBuffer);
					}
				} else {
					connection.systemInput().push("ok");
					setIdentifier(identifier);
				}
			} else if (message.toLowerCase().equals("ok")) {
				future.complete("ok");
			} else {
				future.complete(builder.toString());
			}
		}
	}
}
