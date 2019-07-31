package com.github.thorbenkuck.network.stream;

import java.util.List;

public class NativeEventStream<T> extends AbstractEventStream<T> {
	@Override
	protected void dispatch(List<NotifiableSubscription<T>> notifiableSubscriptions, T t) {
		notifiableSubscriptions.forEach(concreteSubscription -> concreteSubscription.notify(t));
	}
}
