package com.github.thorbenkuck.network.stream;

import java.util.List;

public interface EventStream<T> {

	Subscription subscribe(Subscriber<T> subscriber);

	List<ConcreteSubscription<T>> getSubscriptions();

}
