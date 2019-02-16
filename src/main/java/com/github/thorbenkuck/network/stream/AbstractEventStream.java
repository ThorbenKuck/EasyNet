package com.github.thorbenkuck.network.stream;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEventStream<T> implements WritableEventStream<T> {

	private final List<ConcreteSubscription<? super T>> subscriptions = new ArrayList<>();

	private void publish(T t) {
		List<ConcreteSubscription<? super T>> copy;

		synchronized (subscriptions) {
			copy = new ArrayList<>(subscriptions);
		}

		dispatch(copy, t);
		copy.clear();
		copy = null; // Just help the GC
	}

	protected abstract void dispatch(List<ConcreteSubscription<? super T>> subscriptions, T t);

	@Override
	public void destroy() {
		List<ConcreteSubscription<? super T>> copy;
		synchronized (subscriptions) {
			copy = new ArrayList<>(subscriptions);
		}

		copy.forEach(Subscription::cancel);
		subscriptions.clear();
	}

	@Override
	public void push(T t) {
		publish(t);
	}

	@Override
	public Subscription<T> subscribe(Subscriber<T> subscriber) {
		ConcreteSubscription<T> subscription = new SimpleSubscription<>(subscriber);
		subscription.connect(this);
		return subscription;
	}

	@Override
	public void addSubscription(ConcreteSubscription<? super T> subscription) {
		subscriptions.add(subscription);
	}

	@Override
	public void addSubscription(Subscription<? super T> subscription) {
		if (!ConcreteSubscription.class.isAssignableFrom(subscription.getClass())) {
			throw new IllegalArgumentException("Only ConcreteSubscription classes can be added!");
		}

		addSubscription((ConcreteSubscription<? super T>) subscription);
	}

	@Override
	public List<ConcreteSubscription<? super T>> getSubscriptions() {
		return subscriptions;
	}
}
