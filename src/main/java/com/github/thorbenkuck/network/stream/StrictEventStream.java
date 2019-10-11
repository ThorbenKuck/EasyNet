package com.github.thorbenkuck.network.stream;

import com.github.thorbenkuck.network.exceptions.EmptySubscriberListException;

import java.util.List;

public class StrictEventStream<T> extends SimpleEventStream<T> {

	@Override
	protected void dispatch(List<NotifiableSubscription<T>> notifiableSubscriptions, T t) {
		if (notifiableSubscriptions.isEmpty()) {
			throw new EmptySubscriberListException("No Subscribers found for " + t);
		}
		super.dispatch(notifiableSubscriptions, t);
	}
}
