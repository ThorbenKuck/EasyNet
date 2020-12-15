package com.github.thorbenkuck.network.stream;

import java.util.List;

public interface Subscription {

	Subscription onError(ExceptionalConsumer<Throwable> consumer);

	boolean isCanceled();

	void cancel();

	void setOnCancel(Runnable runnable);

	List<Throwable> drainEncountered();

	boolean hasEncounteredErrors();

	void preventErrorBuffer();

}
