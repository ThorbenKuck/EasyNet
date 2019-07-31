package com.github.thorbenkuck.network.stream;

import java.util.List;

public class ParallelEventStream<T> extends AbstractEventStream<T> {
	@Override
	protected void dispatch(List<NotifiableSubscription<T>> notifiableSubscriptions, T t) {
		notifiableSubscriptions.parallelStream()
				.forEach(sub -> sub.notify(t));
	}
}
