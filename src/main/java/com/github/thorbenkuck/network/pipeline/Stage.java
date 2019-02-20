package com.github.thorbenkuck.network.pipeline;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Stage<T> {

	<S> Stage<S> add(Function<T, S> function);

	Stage<T> add(Consumer<T> consumer);

}
