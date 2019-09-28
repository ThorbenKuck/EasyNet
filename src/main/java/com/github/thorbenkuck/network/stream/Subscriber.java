package com.github.thorbenkuck.network.stream;

@FunctionalInterface
public interface Subscriber<T> {

    void accept(T t) throws Exception;

    default void onCancel() {
    }

    default void onSubscribe() {
    }

    default void onError(Throwable throwable) throws Throwable {
        throw throwable;
    }

}
