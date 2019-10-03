package com.github.thorbenkuck.network.exceptions;

public class NetworkRuntimeException extends RuntimeException {

    public NetworkRuntimeException() {
    }

    public NetworkRuntimeException(String message) {
        super(message);
    }

    public NetworkRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkRuntimeException(Throwable cause) {
        super(cause);
    }
}
