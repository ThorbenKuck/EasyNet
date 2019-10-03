package com.github.thorbenkuck.network.exceptions;

public class EmptySubscriberListException extends NetworkRuntimeException {

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
}
