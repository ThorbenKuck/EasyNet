package com.github.thorbenkuck.network.stream;

public interface WritableEventStream<T> extends DataStream<T> {

	void close();

	void pause();

	void unPause();

	boolean isPaused();

	boolean isDisabled();

	void setDisabled(boolean disabled);
}
