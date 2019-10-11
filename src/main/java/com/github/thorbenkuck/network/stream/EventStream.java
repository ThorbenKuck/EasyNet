package com.github.thorbenkuck.network.stream;

public interface EventStream<T> {

    static <T> EventStream<T> wrap(EventStream<T> eventStream) {
        if (!(eventStream instanceof ManagedEventStream)) {
            throw new IllegalArgumentException("A ManagedEventStream is required!");
        }
        return new DelegatingEventStream<T>((ManagedEventStream<T>) eventStream);
    }

    static <T> EventStream<T> readFrom(DataStream<T> dataStream) {
        ManagedEventStream<T> managedEventStream = ManagedEventStream.sequential();
        dataStream.subscribe(managedEventStream::push);
        return managedEventStream;
    }

    Subscription subscribe(Subscriber<T> subscriber);

    default Subscription endIn(Sink<? super T> sink) {
        return subscribe(sink::push);
    }

    default Subscription branchTo(DataStream<? super T> eventStream) {
        return subscribe(eventStream::push);
    }

}
