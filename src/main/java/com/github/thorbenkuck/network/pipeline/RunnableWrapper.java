package com.github.thorbenkuck.network.pipeline;

import java.util.function.Function;

public class RunnableWrapper<T> implements Function<T, T> {

	private final Runnable runnable;

	public RunnableWrapper(Runnable runnable) {
		this.runnable = runnable;
	}

	@Override
	public T apply(T t) {
		runnable.run();
		return t;
	}
}
