package com.github.thorbenkuck.network.pipeline;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Pipeline<T> {

	private final List<Branch<T>> branches = new LinkedList<>();

	public <S> Stage<S> add(Function<T, S> function) {
		Branch<T> branch = new Branch<>();
		branches.add(branch);
		return branch.add(function);
	}

	public Stage<T> add(Consumer<T> consumer) {
		Branch<T> branch = new Branch<>();
		branches.add(branch);
		return branch.add(consumer);
	}

	public Stage<T> add(Runnable runnable) {
		Branch<T> branch = new Branch<>();
		branches.add(branch);
		return branch.add(runnable);
	}

	public void apply(T t) {
		List<Branch<T>> branches = new ArrayList<>(this.branches);

		branches.forEach(branch -> branch.propagate(t));

		branches.clear();
	}

}
