package com.github.thorbenkuck.network.connection;

import com.github.thorbenkuck.network.RawData;
import com.github.thorbenkuck.network.Session;
import com.github.thorbenkuck.network.stream.EventStream;
import com.github.thorbenkuck.network.stream.DataStream;
import com.github.thorbenkuck.network.stream.SimpleEventStream;
import com.github.thorbenkuck.network.stream.WritableEventStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

class TCPConnection implements Connection {

	private final Socket socket;
	private final DataInputStream dataInputStream;
	private final DataOutputStream dataOutputStream;
	private final WritableEventStream<byte[]> output = new SimpleEventStream<>();
	private final WritableEventStream<byte[]> inputStream = new SimpleEventStream<>();
	private final ReadingService readingService = new ReadingService();
	private final Thread thread = new Thread(readingService);
	private Consumer<Connection> onDisconnect;
	private Session session;

	TCPConnection(Socket socket) throws IOException {
		this.socket = socket;
		this.dataInputStream = new DataInputStream(socket.getInputStream());
		this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
		this.inputStream.subscribe(this::writeSilent);
	}

	@Override
	public DataStream<byte[]> inputStream() {
		return inputStream;
	}

	@Override
	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public void listen() {
		if (readingService.running) {
			return;
		}
		thread.setName("TCP Listener");
		thread.start();
	}

	@Override
	public void writeSilent(byte[] data) {
		try {
			write(data);
		} catch (IOException ignored) {
		}
	}

	@Override
	public synchronized void write(byte[] data) throws IOException {
		dataOutputStream.writeInt(data.length);
		dataOutputStream.write(data);
		dataOutputStream.flush();
	}

	@Override
	public void writeSilent(RawData data) {
		writeSilent(data.getBytes());
	}

	@Override
	public synchronized void write(RawData data) throws IOException {
		write(data.getBytes());
	}

	@Override
	public EventStream<byte[]> outputStream() {
		return output;
	}

	@Override
	public void closeSilently() {
		try {
			socket.close();
		} catch (IOException ignored) {
		}
		try {
			dataOutputStream.close();
		} catch (IOException ignored) {
		}
		try {
			dataInputStream.close();
		} catch (IOException ignored) {
		}
		readingService.running = false;
	}

	@Override
	public void close() throws IOException {
		readingService.running = false;
		socket.close();
		dataOutputStream.close();
		dataInputStream.close();
	}

	@Override
	public Session session() {
		return session;
	}

	@Override
	public String remoteAddress() {
		return socket.getRemoteSocketAddress().toString();
	}

	@Override
	public Consumer<Connection> getOnDisconnect() {
		return onDisconnect;
	}

	@Override
	public void setOnDisconnect(Consumer<Connection> onDisconnect) {
		this.onDisconnect = onDisconnect;
	}

	private final class ReadingService implements Runnable {

		private boolean running = false;

		@Override
		public void run() {
			running = true;
			while (socket.isConnected() && running) {
				int size;
				try {
					size = dataInputStream.readInt();
					byte[] buffer = new byte[size];
					dataInputStream.readFully(buffer);
					output.push(buffer);
					// Helping the GC to collect it!
					// This is needed, on servers with
					// very little heap space.
					buffer = null;
				} catch (IOException e) {
					// EOF reached. No print needed
					running = false;
					closeSilently();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (Throwable throwable) {
					throwable.printStackTrace();
					writeSilent("!ACCESS DENIED!".getBytes());
				}
			}

			running = false;
			Consumer<Connection> onDisconnect = getOnDisconnect();
			if (onDisconnect != null) {
				onDisconnect.accept(TCPConnection.this);
			}
		}
	}
}
