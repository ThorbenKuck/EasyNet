package com.github.thorbenkuck.network.stream;

import java.util.function.Consumer;

public interface Source<T> {

  static <T> Source<T> create() {
    return new DistributingSource<>();
  }

  default void onEmit(Consumer<T> consumer) {
    onEmit(new SourceConsumer<>() {
      @Override
      public void onCancel() {}

      @Override
      public void accept(T t) {
        consumer.accept(t);
      }
    });
  }

  void push(T t);

  void onEmit(SourceConsumer<T> consumer);

  void close();

}
