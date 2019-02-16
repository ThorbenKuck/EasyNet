package com.github.thorbenkuck.network.stream;

import java.util.List;

public class SimpleEventStream<T> extends AbstractEventStream<T> {
	@Override
	protected void dispatch(List<ConcreteSubscription<? super T>> concreteSubscriptions, T t) {
		concreteSubscriptions.forEach(concreteSubscription -> concreteSubscription.notify(t));
	}
}
