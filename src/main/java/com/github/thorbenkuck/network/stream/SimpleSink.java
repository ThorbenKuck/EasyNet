package com.github.thorbenkuck.network.stream;

import java.util.function.Consumer;

class SimpleSink<T> implements Sink<T> {

  private final Consumer<T> consumer;

  SimpleSink(Consumer<T> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void push(T t) {
    consumer.accept(t);
  }
}
