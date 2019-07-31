package com.github.thorbenkuck.network.stream;

public interface DataStream<T> extends EventStream<T> {

	void push(T t);

}
