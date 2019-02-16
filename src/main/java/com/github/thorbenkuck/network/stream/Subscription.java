package com.github.thorbenkuck.network.stream;

import java.util.List;

public interface Subscription<T> {

	boolean isCanceled();

	void cancel();

	void setOnCancel(Runnable runnable);

	List<Throwable> drainEncounteredErrors();

	boolean hasEncounteredErrors();

}
