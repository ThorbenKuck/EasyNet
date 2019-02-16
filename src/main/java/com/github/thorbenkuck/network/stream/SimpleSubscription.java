package com.github.thorbenkuck.network.stream;

import java.util.ArrayList;
import java.util.List;

public class SimpleSubscription<T> implements ConcreteSubscription<T> {

	private final Subscriber<T> subscriber;
	private final List<Throwable> throwableList = new ArrayList<>();
	private final Object lock = new Object();
	private final Object cancelLock = new Object();
	private final Object subscriptionLock = new Object();
	private List<ConcreteSubscription<? super T>> subscriptions;
	private Runnable onCancel;

	public SimpleSubscription(Subscriber<T> subscriber, List<ConcreteSubscription<? super T>> subscriptions) {
		this.subscriber = subscriber;
		this.subscriptions = subscriptions;
	}

	public SimpleSubscription(Subscriber<T> subscriber) {
		this.subscriber = subscriber;
	}

	private void addThrowable(Throwable throwable) {
		synchronized (throwableList) {
			throwableList.add(throwable);
		}
	}

	@Override
	public void connect(EventStream<T> eventStream) {
		eventStream.addSubscription(this);
		synchronized (subscriptionLock) {
			this.subscriptions = eventStream.getSubscriptions();
		}
	}

	@Override
	public void notify(T t) {
		synchronized (lock) {
			if (!isCanceled()) {
				try {
					subscriber.accept(t);
				} catch (Throwable throwable) {
					addThrowable(throwable);
				}
			}
		}
	}

	@Override
	public boolean isCanceled() {
		synchronized (subscriptionLock) {
			return !subscriptions.contains(this);
		}
	}

	@Override
	public void cancel() {
		if (isCanceled()) {
			return;
		}
		synchronized (lock) {
			synchronized (subscriptionLock) {
				subscriptions.remove(this);
				subscriptions = null;
			}
		}

		synchronized (cancelLock) {
			if (onCancel != null) {
				try {
					onCancel.run();
				} catch (Throwable t) {
					addThrowable(t);
				}
				onCancel = null;
			}
		}
	}

	@Override
	public void setOnCancel(Runnable runnable) {
		synchronized (cancelLock) {
			this.onCancel = runnable;
		}
	}

	@Override
	public List<Throwable> drainEncounteredErrors() {
		List<Throwable> toReturn;
		synchronized (throwableList) {
			toReturn = new ArrayList<>(throwableList);
		}
		throwableList.clear();
		return toReturn;
	}

	@Override
	public boolean hasEncounteredErrors() {
		synchronized (throwableList) {
			return !throwableList.isEmpty();
		}
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("SimpleSubscription{");
		sb.append("subscriber=").append(subscriber);
		sb.append(", throwableList=").append(throwableList);
		sb.append(", onCancel=").append(onCancel);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public String prettyPrint() {
		final StringBuffer sb = new StringBuffer("SimpleSubscription{");
		sb.append("subscriber=").append(subscriber);
		sb.append(", throwableList=").append(throwableList);
		sb.append(", onCancel=").append(onCancel);
		sb.append(", subscriptions=").append(subscriptions);
		sb.append('}');
		return sb.toString();
	}
}
