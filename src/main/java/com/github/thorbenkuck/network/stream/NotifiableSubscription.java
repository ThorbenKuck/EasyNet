package com.github.thorbenkuck.network.stream;

public interface NotifiableSubscription<T> extends Subscription {

    NotifiableSubscription<T> onError(ExceptionalConsumer<Throwable> consumer);

    void notify(T t);

    void notify(Throwable throwable);
}
