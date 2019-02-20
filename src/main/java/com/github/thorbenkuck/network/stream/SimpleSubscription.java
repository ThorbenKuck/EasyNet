package com.github.thorbenkuck.network.stream;

import java.util.ArrayList;
import java.util.List;

class SimpleSubscription<T> implements ConcreteSubscription<T> {

	private final Subscriber<T> subscriber;
	private final List<Throwable> throwableBuffer = new ArrayList<>();
	private final Object lock = new Object();
	private final Object cancelLock = new Object();
	private final Object referenceLock = new Object();
	private Reference<ConcreteSubscription<T>> reference;
	private Runnable onCancel;

	SimpleSubscription(Subscriber<T> subscriber, Reference<ConcreteSubscription<T>> reference) {
		this.subscriber = subscriber;
		this.reference = reference;
	}

	private void addThrowable(Throwable throwable) {
		synchronized (throwableBuffer) {
			throwableBuffer.add(throwable);
		}
	}

	@Override
	public void notify(T t) {
		synchronized (lock) {
			if (!isCanceled()) {
				try {
					subscriber.accept(t);
				} catch (Throwable throwable) {
					throwable.printStackTrace();
					addThrowable(throwable);
				}
			}
		}
	}

	@Override
	public boolean isCanceled() {
		synchronized (referenceLock) {
			return reference != null && !reference.contains(this);
		}
	}

	@Override
	public void cancel() {
		if (isCanceled()) {
			return;
		}
		synchronized (referenceLock) {
			if (reference != null) {
				reference.remove(this);
				reference = null;
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
	public List<Throwable> drainEncountered() {
		List<Throwable> toReturn;
		synchronized (throwableBuffer) {
			toReturn = new ArrayList<>(throwableBuffer);
		}
		throwableBuffer.clear();
		return toReturn;
	}

	@Override
	public boolean hasEncounteredErrors() {
		synchronized (throwableBuffer) {
			return !throwableBuffer.isEmpty();
		}
	}

	@Override
	public String toString() {
		return "SimpleSubscription{" + "subscriber=" + subscriber +
				", throwableBuffer=" + throwableBuffer +
				", onCancel=" + onCancel +
				'}';
	}
}
