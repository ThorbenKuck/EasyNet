package com.github.thorbenkuck.network.pipeline;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

public class Branch<T> {

	private final Queue<Function> callQueue = new LinkedList<>();

	public <S> Stage<S> add(Function<T, S> function) {
		callQueue.add(function);
		return new PipelineStage<>(callQueue);
	}

	public Stage<T> add(Consumer<T> consumer) {
		callQueue.add(new ConsumerWrapper<>(consumer));
		return new PipelineStage<>(callQueue);
	}

	public Stage<T> add(Runnable runnable) {
		callQueue.add(new RunnableWrapper<>(runnable));
		return new PipelineStage<>(callQueue);
	}

	public void propagate(T t) {
		Queue<Function> copy = new LinkedList<>(callQueue);
		Function current;
		Object object = t;

		while (copy.peek() != null) {
			try {
				current = copy.poll();
				object = current.apply(object);
			} catch (ClassCastException c) {
				throw new IllegalStateException("[UNRECOVERABLE] Illegal Stage combination!", c);
			}
		}
	}

}