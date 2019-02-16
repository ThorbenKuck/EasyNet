package com.github.thorbenkuck.network.stream;

public interface DataStream<T> {

	void push(T t);

}
