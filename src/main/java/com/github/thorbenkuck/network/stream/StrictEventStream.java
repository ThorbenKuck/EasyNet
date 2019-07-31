package com.github.thorbenkuck.network.stream;

import com.github.thorbenkuck.network.exceptions.EmptySubscriberListException;

import java.util.List;

public class StrictEventStream<T> extends AbstractEventStream<T> {

	@Override
	protected void dispatch(List<NotifiableSubscription<T>> notifiableSubscriptions, T t) {
		if (notifiableSubscriptions.isEmpty()) {
			throw new EmptySubscriberListException("No Subscribers found for " + t);
		}

		notifiableSubscriptions.forEach(concreteSubscription -> concreteSubscription.notify(t));
	}
}
