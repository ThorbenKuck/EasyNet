package com.github.thorbenkuck.network.connection;

import com.github.thorbenkuck.network.DataConnection;
import com.github.thorbenkuck.network.SizeFirstProtocol;

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
		systemOutput().subscribe(b -> System.out.println("[System, Receive]: " + b));
		systemInput.subscribe(b -> System.out.println("[System, Send]: " + b));
	}

	protected synchronized void rawWrite(byte[] data) throws IOException {
		dataConnection.write(data);
		dataConnection.flush();
	}

	protected synchronized void write(byte[] data) throws IOException {
		synchronized (protocolLock) {
			getProtocol().write(data, getDataConnection());
		}
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

	private final class ReadingService implements Runnable {

		private boolean running = false;

		@Override
		public void run() {
			running = true;
			while (socket.isConnected() && running) {
				try {
					byte[] data = readFromProtocol();
					if (data.length < 200) {
						String potential = new String(data);
						if (potential.toLowerCase().startsWith("sys")) {
							systemOutput.push(potential.substring(4));
						} else {
							potential = null;
							output.push(data);
						}
					} else {
						output.push(data);
					}
					// Helping the GC to collect it!
					// This is needed, on servers with
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
			disconnectEvent();
		}

		@Override
		public String toString() {
			return "ReadingService {" +
					"running=" + running +
					'}';
		}
	}
}
