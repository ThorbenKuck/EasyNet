package com.github.thorbenkuck.network.stream;

public interface ExceptionalConsumer<T> {

    void accept(T t) throws Throwable;

}
