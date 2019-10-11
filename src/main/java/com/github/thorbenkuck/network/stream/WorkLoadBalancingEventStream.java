package com.github.thorbenkuck.network.stream;

import java.util.List;

public class WorkLoadBalancingEventStream<T> extends AbstractEventStream<T> {

  private int pointer = 0;

  private int calculateNextPointer(int size) {
    int potentialNext = ++pointer;
    if(potentialNext >= size) {
      return 0;
    }

    return potentialNext;
  }

  @Override
  protected void dispatch(List<NotifiableSubscription<T>> notifiableSubscriptions, T t) {
    int next = calculateNextPointer(notifiableSubscriptions.size());
    NotifiableSubscription<T> tNotifiableSubscription = notifiableSubscriptions.get(next);
    tNotifiableSubscription.notify(t);
    pointer = next;
  }
}
