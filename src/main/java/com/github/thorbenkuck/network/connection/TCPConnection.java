package com.github.thorbenkuck.network.connection;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

class TCPConnection extends AbstractConnection {

	private final ReadingService readingService = new ReadingService();
	private final Thread thread = new Thread(readingService);
	private final DataConnection dataConnection;
	private final Socket socket;

	TCPConnection(Socket socket) throws IOException {
		dataConnection = DataConnection.wrap(socket);
		setProtocol(new SizeFirstProtocol());
		this.socket = socket;
		socket.setKeepAlive(true);
		pipeInputStreams();
	}

	@Override
	public DataConnection getDataConnection() {
		return dataConnection;
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
	public void closeSilently() {
		try {
			super.close();
		} catch (IOException ignored) {
		}
		dataConnection.closeSilent();
		readingService.running = false;
	}

	@Override
	public void close() throws IOException {
		super.close();
		readingService.running = false;
		dataConnection.close();
	}

	@Override
	public SocketAddress localAddress() {
		return socket.getLocalSocketAddress();
	}

	@Override
	public SocketAddress remoteAddress() {
		return socket.getRemoteSocketAddress();
	}

	@Override
	public boolean isOpen() {
		return socket.isConnected();
	}

	@Override
	public String toString() {
		return "TCPConnection{" +
				"readingService=" + readingService +
				'}';
	}

	void received(byte[] data) {
		if (data.length < 200) {
			String potential = new String(data);
			if (potential.toLowerCase().startsWith("sys")) {
				systemOutput.push(potential.substring(4));
			} else {
				output.push(data);
			}
		} else {
			output.push(data);
		}
	}

	private final class ReadingService implements Runnable {

		private boolean running = false;

		@Override
		public void run() {
			running = true;
			while (socket.isConnected() && running) {
				try {
					byte[] data = readFromProtocol();
					received(data);
					// Helping the GC to collect it!
					// This helps, on servers with
					// very little heap space, to
					// squeeze the last little bit of
					// performance out of this puppy
					data = null;
				} catch (IOException e) {
					// EOF reached. No print needed
					running = false;
					closeSilently();
				} catch (IllegalStateException ignored) {
				} catch (Throwable throwable) {
					acceptError(throwable);
				}
			}

			running = false;
			triggerDisconnectEvent();
		}

		@Override
		public String toString() {
			return "ReadingService {" +
					"running=" + running +
					'}';
		}
	}
}
