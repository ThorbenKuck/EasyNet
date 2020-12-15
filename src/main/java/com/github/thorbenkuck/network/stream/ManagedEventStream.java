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

    static <T> ManagedEventStream<T> balanced() {
        return balanced(false);
    }

    static <T> ManagedEventStream<T> balanced(boolean async) {
        return new WorkLoadBalancingEventStream<>(async);
    }

    static <T> ManagedEventStream<T> sequential(Source<? extends T> source) {
        ManagedEventStream<T> managedEventStream = sequential();
        source.onEmit(managedEventStream::push);
        return managedEventStream;
    }

    static <T> ManagedEventStream<T> strict(Source<? extends T> source) {
        ManagedEventStream<T> managedEventStream = strict();
        source.onEmit(managedEventStream::push);
        return managedEventStream;
    }

    static <T> ManagedEventStream<T> parallel(Source<? extends T> source) {
        ManagedEventStream<T> managedEventStream = parallel();
        source.onEmit(managedEventStream::push);
        return managedEventStream;
    }

    static <T> ManagedEventStream<T> balanced(Source<? extends T> source) {
        ManagedEventStream<T> managedEventStream = balanced();
        source.onEmit(managedEventStream::push);
        return managedEventStream;
    }

    static <T> ManagedEventStream<T> balanced(boolean async, Source<? extends T> source) {
        ManagedEventStream<T> managedEventStream = balanced(async);
        source.onEmit(managedEventStream::push);
        return managedEventStream;
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
