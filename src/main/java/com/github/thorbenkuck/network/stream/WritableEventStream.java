package com.github.thorbenkuck.network.stream;

public interface WritableEventStream<T> extends EventStream<T>, DataStream<T> {

	void destroy();
}
