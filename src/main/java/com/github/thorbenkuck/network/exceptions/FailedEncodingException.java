package com.github.thorbenkuck.network.exceptions;

public class FailedEncodingException extends Exception {

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

	public FailedEncodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
