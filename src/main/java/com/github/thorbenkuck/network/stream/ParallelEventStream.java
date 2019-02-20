package com.github.thorbenkuck.network.stream;

import java.util.List;

public class ParallelEventStream<T> extends AbstractEventStream<T> {
	@Override
	protected void dispatch(List<ConcreteSubscription<T>> concreteSubscriptions, T t) {
		concreteSubscriptions.parallelStream()
				.forEach(sub -> sub.notify(t));
	}
}
