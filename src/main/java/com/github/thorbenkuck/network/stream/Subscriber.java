package com.github.thorbenkuck.network.stream;

@FunctionalInterface
public interface Subscriber<T> {

	void accept(T t);

}
