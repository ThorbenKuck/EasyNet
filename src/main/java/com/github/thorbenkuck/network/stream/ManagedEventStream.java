package com.github.thorbenkuck.network.stream;

import java.util.List;

public interface ManagedEventStream<T> extends DataStream<T> {

    static <T> ManagedEventStream<T> wrap(ManagedEventStream<T> eventStream) {
        return new DelegatingEventStream<T>(eventStream);
    }

    static <T> ManagedEventStream<T> sequential() {
        return new SimpleEventStream<>();
    }

    static <T> ManagedEventStream<T> strict() {
        return new StrictEventStream<>();
    }

    static <T> ManagedEventStream<T> parallel() {
        return new ParallelEventStream<>();
    }

    List<NotifiableSubscription<T>> getSubscriptions();

    void clearSubscribers();

    void close();

    void pause();

    void unPause();

    boolean isPaused();

    boolean isDisabled();

    void setDisabled(boolean disabled);
}
