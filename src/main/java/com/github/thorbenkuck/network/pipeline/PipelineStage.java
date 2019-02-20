package com.github.thorbenkuck.network.pipeline;

import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

class PipelineStage<T> implements Stage<T> {

	private final Queue<Function> callQueue;

	PipelineStage(Queue<Function> callQueue) {
		this.callQueue = callQueue;
	}

	@Override
	public <S> Stage<S> add(Function<T, S> function) {
		callQueue.add(function);

		return new PipelineStage<>(callQueue);
	}

	@Override
	public Stage<T> add(Consumer<T> consumer) {
		callQueue.add(new ConsumerWrapper<>(consumer));

		return this;
	}
}
