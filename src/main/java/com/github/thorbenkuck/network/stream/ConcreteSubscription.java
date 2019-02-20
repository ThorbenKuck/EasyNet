package com.github.thorbenkuck.network.stream;

public interface ConcreteSubscription<T> extends Subscription {

	void notify(T t);
}
