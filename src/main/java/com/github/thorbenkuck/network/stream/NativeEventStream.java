package com.github.thorbenkuck.network.stream;

import java.util.List;

public class NativeEventStream<T> extends AbstractEventStream<T> {
	@Override
	protected void dispatch(List<ConcreteSubscription<T>> concreteSubscriptions, T t) {
		concreteSubscriptions.forEach(concreteSubscription -> concreteSubscription.notify(t));
	}
}
