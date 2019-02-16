package com.github.thorbenkuck;

import com.github.thorbenkuck.network.server.ServerContainer;
import com.github.thorbenkuck.network.stream.Subscription;

public class ServerTest {

	public static void main(String[] args) throws Exception {
		ServerContainer serverContainer = ServerContainer.open(9999);
		Subscription printSubscription = serverContainer.output().subscribe(remoteMessage -> System.out.println(remoteMessage.getDataObject()));
		Subscription sendSubscription = serverContainer.output().subscribe(remoteMessage -> remoteMessage.getConnection().writeSilent(serverContainer.getObjectEncoder().apply(remoteMessage.getDataObject())));
		serverContainer.accept();
		printSubscription.cancel();
		sendSubscription.cancel();
	}

}
