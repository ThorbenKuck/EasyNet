package com.github.thorbenkuck.network.stream;

public interface ConcreteSubscription<T> extends Subscription<T> {

	void connect(EventStream<T> eventStream);

	void notify(T t);

	String prettyPrint();
}
