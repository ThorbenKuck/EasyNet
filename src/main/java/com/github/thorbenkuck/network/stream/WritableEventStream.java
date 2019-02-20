package com.github.thorbenkuck.network.stream;

public interface WritableEventStream<T> extends EventStream<T>, DataStream<T> {

	void pause();

	void cut();

	void unPause();

	boolean isPaused();

	boolean isDisabled();

	void setDisabled(boolean disabled);
}
