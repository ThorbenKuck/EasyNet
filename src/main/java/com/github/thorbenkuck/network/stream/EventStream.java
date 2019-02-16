package com.github.thorbenkuck.network.stream;

import java.util.List;

public interface EventStream<T> {

	Subscription subscribe(Subscriber<T> subscriber);

	List<ConcreteSubscription<? super T>> getSubscriptions();

	void addSubscription(ConcreteSubscription<? super T> subscription);

	void addSubscription(Subscription<? super T> subscription);

}
