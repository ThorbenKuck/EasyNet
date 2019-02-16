package com.github.thorbenkuck.network.pipeline;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

public class Pipeline<T> {

	private final Queue<Function> callQueue = new LinkedList<>();

	public <S> DynamicPipelineConnection<S> add(Function<T, S> function) {
		callQueue.add(function);
		return new PipelineConnection<>(callQueue);
	}

	public DynamicPipelineConnection<T> add(Consumer<T> consumer) {
		callQueue.add(new FunctionWrapper<>(consumer));
		return new PipelineConnection<>(callQueue);
	}

	public void run(T t) {
		Queue<Function> copy = new LinkedList<>(callQueue);
		Function current;
		Object object = t;

		while(copy.peek() != null) {
			object = copy.poll().apply(object);
		}
	}

}
