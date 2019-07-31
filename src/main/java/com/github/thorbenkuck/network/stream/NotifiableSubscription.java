package com.github.thorbenkuck.network.stream;

public interface NotifiableSubscription<T> extends Subscription {

	void notify(T t);
}
