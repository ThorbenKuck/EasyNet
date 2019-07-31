package com.github.thorbenkuck.network.stream;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEventStream<T> implements WritableEventStream<T> {

	private final List<NotifiableSubscription<T>> subscriptions = new ArrayList<>();
	private final List<T> buffer = new ArrayList<>();
	private final EventStreamReference reference = new EventStreamReference();
	private boolean paused = false;
	private boolean disabled = false;

	private void publish(T t) {
		List<NotifiableSubscription<T>> copy;

		synchronized (subscriptions) {
			copy = new ArrayList<>(subscriptions);
		}

		dispatch(copy, t);
		copy.clear();
		copy = null; // Just help the GC
	}

	protected abstract void dispatch(List<NotifiableSubscription<T>> subscriptions, T t);

	@Override
	public synchronized void unPause() {
		paused = false;
		buffer.forEach(this::publish);
		buffer.clear();
	}

	@Override
	public synchronized void pause() {
		paused = true;
	}

	@Override
	public void close() {
		List<NotifiableSubscription<? super T>> copy;
		synchronized (subscriptions) {
			copy = new ArrayList<>(subscriptions);
		}

		copy.forEach(Subscription::cancel);
		buffer.clear();
		setDisabled(true);
		pause();
		subscriptions.clear();
	}

	@Override
	public boolean isPaused() {
		return paused;
	}

	@Override
	public void push(T t) {
		if (isDisabled()) {
			return;
		}

		if (isPaused()) {
			buffer.add(t);
		} else {
			publish(t);
		}
	}

	@Override
	public Subscription subscribe(Subscriber<T> subscriber) {
		NotifiableSubscription<T> subscription = new SimpleSubscription<>(subscriber, reference);
		subscriptions.add(subscription);
		return subscription;
	}

	@Override
	public List<NotifiableSubscription<T>> getSubscriptions() {
		return subscriptions;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	private final class EventStreamReference implements Reference<NotifiableSubscription<T>> {

		@Override
		public boolean contains(NotifiableSubscription<T> subscription) {
			return subscriptions.contains(subscription);
		}

		@Override
		public void remove(NotifiableSubscription<T> subscription) {
			subscriptions.remove(subscription);
		}
	}
}
