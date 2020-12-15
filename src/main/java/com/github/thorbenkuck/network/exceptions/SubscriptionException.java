package com.github.thorbenkuck.network.exceptions;

public class SubscriptionException extends RuntimeException {

    public SubscriptionException(Throwable cause) {
        super("Encountered " + cause.getClass().getName() + " while subscription handling: " + cause.getMessage(), cause);
    }
}
