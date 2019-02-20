package com.github.thorbenkuck.network.stream;

import com.github.thorbenkuck.network.stream.exception.EmptySubscriberListException;

import java.util.List;

public class StriktEventStream<T> extends AbstractEventStream<T> {

	@Override
	protected void dispatch(List<ConcreteSubscription<T>> concreteSubscriptions, T t) {
		if (concreteSubscriptions.isEmpty()) {
			throw new EmptySubscriberListException("No Subscribers found for " + t);
		}

		concreteSubscriptions.forEach(concreteSubscription -> concreteSubscription.notify(t));
	}
}
