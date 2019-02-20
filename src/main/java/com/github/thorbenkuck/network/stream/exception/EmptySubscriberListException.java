package com.github.thorbenkuck.network.stream.exception;

public class EmptySubscriberListException extends RuntimeException {

	public EmptySubscriberListException() {
	}

	public EmptySubscriberListException(String message) {
		super(message);
	}

	public EmptySubscriberListException(String message, Throwable cause) {
		super(message, cause);
	}

	public EmptySubscriberListException(Throwable cause) {
		super(cause);
	}

	public EmptySubscriberListException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
