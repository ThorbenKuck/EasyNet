package com.github.thorbenkuck.network.stream;

public interface Reference<T> {

	boolean contains(T object);

	void remove(T t);

}
