package com.github.thorbenkuck;

import com.github.thorbenkuck.network.exceptions.EmptySubscriberListException;
import com.github.thorbenkuck.network.stream.StrictEventStream;
import com.github.thorbenkuck.network.stream.Subscription;
import com.github.thorbenkuck.network.stream.WritableEventStream;

public class StreamTest {

	public static void main(String[] args) {
		WritableEventStream<TestObject> stream = new StrictEventStream<>();
		Subscription subscription = stream.subscribe(System.out::println);
		subscription.setOnCancel(() -> System.out.println("Canceled"));

		System.out.println(stream.getSubscriptions());

        stream.push(new TestObject("message"));
		stream.push(new TestObject2());
		subscription.cancel();
		try {
            stream.push(new TestObject("message"));
		} catch (EmptySubscriberListException e) {
			e.printStackTrace(System.out);
		}
        System.out.println(subscription.isCanceled());
	}

	private static final class TestObject2 extends TestObject {
        private TestObject2() {
            super("message");
        }
    }

}
