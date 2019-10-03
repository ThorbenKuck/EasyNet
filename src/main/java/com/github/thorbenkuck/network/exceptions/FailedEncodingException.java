package com.github.thorbenkuck.network.exceptions;

public class FailedEncodingException extends NetworkException {

	public FailedEncodingException() {
	}

	public FailedEncodingException(String message) {
		super(message);
	}

	public FailedEncodingException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailedEncodingException(Throwable cause) {
		super(cause);
	}
}
