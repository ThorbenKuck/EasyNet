package com.github.thorbenkuck;

import com.github.thorbenkuck.network.client.ClientContainer;
import com.github.thorbenkuck.network.stream.Subscription;

public class ClientTest {

	public static void main(String[] args) throws Exception {
		ClientContainer clientContainer = ClientContainer.open("localhost", 9999);
		Subscription subscription = clientContainer.output().subscribe(remoteMessage -> System.out.println(remoteMessage.getDataObject()));
		subscription.setOnCancel(() -> System.out.println("Disconnected!"));
		clientContainer.listen();
		clientContainer.send(new TestObject());
	}

}
