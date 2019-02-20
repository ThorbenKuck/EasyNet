package com.github.thorbenkuck;

import com.github.thorbenkuck.network.stream.StriktEventStream;
import com.github.thorbenkuck.network.stream.Subscription;
import com.github.thorbenkuck.network.stream.WritableEventStream;
import com.github.thorbenkuck.network.stream.exception.EmptySubscriberListException;

public class StreamTest {

	public static void main(String[] args) {
		WritableEventStream<TestObject> stream = new StriktEventStream<>();
		Subscription subscription = stream.subscribe(System.out::println);
		subscription.setOnCancel(() -> System.out.println("Canceled"));

		System.out.println(stream.getSubscriptions());

		stream.push(new TestObject());
		stream.push(new TestObject2());
		subscription.cancel();
		try {
			stream.push(new TestObject());
		} catch (EmptySubscriberListException e) {
			e.printStackTrace(System.out);
		}
	}

	private static final class TestObject2 extends TestObject {
	}

}
