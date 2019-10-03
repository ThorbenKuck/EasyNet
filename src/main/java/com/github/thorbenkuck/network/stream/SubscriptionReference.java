package com.github.thorbenkuck.network.stream;

public interface SubscriptionReference<T> {

	boolean contains(T object);

	void remove(T t);

}
