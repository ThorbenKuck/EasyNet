package com.github.thorbenkuck.network.stream;

import java.util.function.Consumer;

public interface Sink<T> {

  static <T> Sink<T> of(Consumer<T> consumer) {
    return new SimpleSink<>(consumer);
  }

  default void attachTo(EventStream<? extends T> eventStream) {
    eventStream.subscribe(this::push);
  }

  void push(T t);

}
