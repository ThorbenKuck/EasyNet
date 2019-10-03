package com.github.thorbenkuck;

import com.github.thorbenkuck.network.exceptions.EmptySubscriberListException;
import com.github.thorbenkuck.network.stream.ManagedEventStream;
import com.github.thorbenkuck.network.stream.Subscriber;
import com.github.thorbenkuck.network.stream.Subscription;

public class StreamTest {

	public static void main(String[] args) {
		ManagedEventStream<TestObject> stream = ManagedEventStream.strict();
		Subscription subscription = stream.subscribe(new CustomSubscriber());

        stream.push(new TestObject("message"));
		stream.pushError(new NullPointerException("This is a test Exception"));
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

	private static final class CustomSubscriber implements Subscriber<TestObject> {

		@Override
		public void accept(TestObject testObject) {
			System.out.println(testObject);
		}

		@Override
		public void onCancel() {
			System.out.println("[CustomSubscriber] canceled");
		}

		@Override
		public void onSubscribe() {
			System.out.println("[CustomSubscriber] just subscribed");
		}

		@Override
		public void onError(Throwable throwable) {
			System.out.println("[CustomSubscriber] " + throwable.getMessage());
		}
	}

}
