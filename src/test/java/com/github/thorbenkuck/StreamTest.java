package com.github.thorbenkuck;

import com.github.thorbenkuck.network.stream.SimpleEventStream;
import com.github.thorbenkuck.network.stream.SimpleSubscription;
import com.github.thorbenkuck.network.stream.WritableEventStream;

public class StreamTest {

	public static void main(String[] args) {
		WritableEventStream<TestObject> stream = new SimpleEventStream<>();
		SimpleSubscription<TestObject> subscription = new SimpleSubscription<>(System.out::println);
		subscription.setOnCancel(() -> System.out.println("Canceled"));

		subscription.connect(stream);

		System.out.println(stream.getSubscriptions());
		System.out.println(subscription.prettyPrint());

		stream.push(new TestObject());
		subscription.cancel();
		stream.push(new TestObject());

		System.out.println(stream.getSubscriptions());
		System.out.println(subscription.prettyPrint());
	}

}
