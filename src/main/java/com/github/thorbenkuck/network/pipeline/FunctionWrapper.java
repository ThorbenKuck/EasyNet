package com.github.thorbenkuck.network.pipeline;

import java.util.function.Consumer;
import java.util.function.Function;

class FunctionWrapper<T> implements Function<T, T> {

	private final Consumer<T> consumer;

	FunctionWrapper(Consumer<T> consumer) {
		this.consumer = consumer;
	}

	@Override
	public T apply(T t) {
		consumer.accept(t);
		return t;
	}
}
