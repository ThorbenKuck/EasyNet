package com.github.thorbenkuck.network.pipeline;

import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

class PipelineConnection<T> implements DynamicPipelineConnection<T> {

	private final Queue<Function> callQueue;

	PipelineConnection(Queue<Function> callQueue) {
		this.callQueue = callQueue;
	}

	@Override
	public <S> DynamicPipelineConnection<S> add(Function<T, S> function) {
		callQueue.add(function);

		return new PipelineConnection<>(callQueue);
	}

	@Override
	public DynamicPipelineConnection<T> add(Consumer<T> consumer) {
		callQueue.add(new FunctionWrapper<>(consumer));

		return this;
	}
}
