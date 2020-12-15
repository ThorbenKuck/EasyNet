package com.github.thorbenkuck.network.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractEventStream<T> implements ManagedEventStream<T> {

	private final List<NotifiableSubscription<T>> subscriptions = new ArrayList<>();
	private final List<T> buffer = new ArrayList<>();
	private final EventStreamSubscriptionReference reference = new EventStreamSubscriptionReference();
	private boolean paused = false;
	private boolean disabled = false;

	private void publish(T t) {
		List<NotifiableSubscription<T>> copy = new ArrayList<>(subscriptions);
		dispatch(copy, t);
	}

	protected abstract void dispatch(List<NotifiableSubscription<T>> subscriptions, T t);

	protected void newSubscription(NotifiableSubscription<T> subscription) {
	}

	protected void deletedSubscription(NotifiableSubscription<T> subscription) {
	}

	private void removeSubscription(NotifiableSubscription<T> subscription) {
		subscriptions.remove(subscription);
		deletedSubscription(subscription);
	}

	private void addSubscription(NotifiableSubscription<T> subscription) {
		subscriptions.add(subscription);
		newSubscription(subscription);
	}

	@Override
	public void clearSubscribers() {
		ArrayList<NotifiableSubscription<T>> notifiableSubscriptions = new ArrayList<>(subscriptions);
		notifiableSubscriptions.forEach(Subscription::cancel);
	}

	@Override
	public void pushError(Throwable throwable) {
		List<NotifiableSubscription<T>> copy = new ArrayList<>(subscriptions);
		copy.forEach(subscription -> subscription.notify(throwable));
		copy.clear();
	}

	@Override
	public void unPause() {
		paused = false;
		buffer.forEach(this::publish);
		buffer.clear();
	}

	@Override
	public void pause() {
		paused = true;
	}

	@Override
	public void close() {
		List<NotifiableSubscription<? super T>> copy = new ArrayList<>(subscriptions);

		pause();
		setDisabled(true);
		copy.forEach(Subscription::cancel);
		subscriptions.clear();
		buffer.clear();
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
		addSubscription(subscription);
		subscriber.onSubscribe();
		return subscription;
	}

	@Override
	public <S> EventStream<S> pipe(Transformation<T, S> transformation, Supplier<DataStream<S>> eventStreamFunction) {
		DataStream<S> eventStream = eventStreamFunction.get();
		subscribe(t -> {
			try {
				S s = transformation.apply(t);
				eventStream.push(s);
			} catch (Exception e) {
				eventStream.pushError(e);
			}
		});
		return eventStream;
	}

	@Override
	public List<NotifiableSubscription<T>> getSubscriptions() {
		return Collections.unmodifiableList(subscriptions);
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	private final class EventStreamSubscriptionReference implements SubscriptionReference<NotifiableSubscription<T>> {

		@Override
		public boolean contains(NotifiableSubscription<T> subscription) {
			return subscriptions.contains(subscription);
		}

		@Override
		public void remove(NotifiableSubscription<T> subscription) {
			removeSubscription(subscription);
		}
	}
}
