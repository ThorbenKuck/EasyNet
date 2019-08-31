package com.github.thorbenkuck;

import com.github.thorbenkuck.network.server.ServerConnectionFactory;
import com.github.thorbenkuck.network.server.ServerContainer;
import com.github.thorbenkuck.network.stream.Subscription;

public class ServerTest {

	public static void main(String[] args) throws Exception {
		try (ServerContainer serverContainer = ServerContainer.open(9999, ServerConnectionFactory.builder()
				.nonBlocking()
//				.afterCreation(serverSocket -> serverSocket.setSoTimeout(0))
//				.afterCreation(serverSocket -> serverSocket.setReuseAddress(true))
//				.afterConnect(socket -> socket.setSoTimeout(0))
//				.afterConnect(socket -> socket.setReuseAddress(true))
//				.afterConnect(socket -> socket.setKeepAlive(true))
				.build())) {

			serverContainer.ingoingConnections().subscribe(context -> {
				context.connection().setOnDisconnect(dc -> System.out.println("Disconnect at " + dc.remoteAddress()));
				System.out.println("Connected to " + context.getIdentifier() + "@" + context.connection().remoteAddress());
			});

			Subscription printSubscription = serverContainer.output().subscribe(remoteMessage -> System.out.println(remoteMessage.getContext().getIdentifier() + ": " + remoteMessage.getData()));
			Subscription sendSubscription = serverContainer.output().subscribe(remoteMessage -> remoteMessage.getContext().inputStream().push(serverContainer.getObjectEncoder().apply(remoteMessage.getData())));
			serverContainer.accept();

			System.out.println("Server done. Closing");

			printSubscription.cancel();
			sendSubscription.cancel();
		}
	}
}
