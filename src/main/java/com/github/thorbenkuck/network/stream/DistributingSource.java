package com.github.thorbenkuck.network.stream;

import java.util.ArrayList;
import java.util.List;

public class DistributingSource<T> implements Source<T> {

  private final List<SourceConsumer<T>> consumers = new ArrayList<>();

  private List<SourceConsumer<T>> copy() {
    List<SourceConsumer<T>> copy;
    synchronized (consumers) {
      copy = new ArrayList<>(consumers);
    }
    return copy;
  }

  @Override
  public void push(T t) {
    List<SourceConsumer<T>> copy = copy();
    copy.forEach(tSourceConsumer -> tSourceConsumer.accept(t));
  }

  @Override
  public void onEmit(SourceConsumer<T> consumer) {
    consumers.add(consumer);
  }

  @Override
  public void close() {
    List<SourceConsumer<T>> copy = copy();
    copy.forEach(SourceConsumer::onCancel);
    consumers.clear();
  }
}
