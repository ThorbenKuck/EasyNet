package com.github.thorbenkuck.network.stream;

public interface DataStream<T> extends EventStream<T> {

	static <T> DataStream<T> wrap(DataStream<T> eventStream) {
		if (!(eventStream instanceof ManagedEventStream)) {
			throw new IllegalArgumentException("A ManagedEventStream is required!");
		}
		return new DelegatingEventStream<T>((ManagedEventStream<T>) eventStream);
	}

	static <T> DataStream<T> sequential() {
		return new SimpleEventStream<>();
	}

	static <T> DataStream<T> strict() {
		return new StrictEventStream<>();
	}

	static <T> DataStream<T> parallel() {
		return new ParallelEventStream<>();
	}

	void push(T t);

	void pushError(Throwable throwable);

}
