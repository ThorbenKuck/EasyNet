package com.github.thorbenkuck.network.exceptions;

public class FailedDecodingException extends NetworkException {

	public FailedDecodingException() {
	}

	public FailedDecodingException(String message) {
		super(message);
	}

	public FailedDecodingException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailedDecodingException(Throwable cause) {
		super(cause);
	}
}
