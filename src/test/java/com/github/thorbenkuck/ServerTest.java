package com.github.thorbenkuck;

import com.github.thorbenkuck.network.server.ServerContainer;
import com.github.thorbenkuck.network.stream.Subscription;

public class ServerTest {

	public static void main(String[] args) throws Exception {
		ServerContainer serverContainer = ServerContainer.open(9999);
		serverContainer.ingoingConnections().subscribe(context -> {
			context.connection().setOnDisconnect(dc -> System.out.println("Disconnect at " + dc.remoteAddress()));
			System.out.println("Connected to " + context.getIdentifier() + "@" + context.connection().remoteAddress());
		});
		Subscription printSubscription = serverContainer.output().subscribe(remoteMessage -> System.out.println(remoteMessage.getContext().getIdentifier() + ": " + remoteMessage.getDataObject()));
		Subscription sendSubscription = serverContainer.output().subscribe(remoteMessage -> remoteMessage.getContext().inputStream().push(serverContainer.getObjectEncoder().apply(remoteMessage.getDataObject())));
		serverContainer.accept();

		System.out.println("Server done. Closing");

		printSubscription.cancel();
		sendSubscription.cancel();
		serverContainer.closeSilently();
	}
}
