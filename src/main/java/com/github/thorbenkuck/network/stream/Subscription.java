package com.github.thorbenkuck.network.stream;

import java.util.List;

public interface Subscription {

	boolean isCanceled();

	void cancel();

	void setOnCancel(Runnable runnable);

	List<Throwable> drainEncountered();

	boolean hasEncounteredErrors();

	void preventErrorBuffer();

}
