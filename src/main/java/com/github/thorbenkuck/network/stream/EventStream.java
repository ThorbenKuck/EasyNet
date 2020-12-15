package com.github.thorbenkuck.network.stream;

import java.util.function.Function;
import java.util.function.Supplier;

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

    <S> EventStream<S> pipe(Transformation<T, S> transformation, Supplier<DataStream<S>> eventStreamFunction);

    default <S> EventStream<S> pipe(Transformation<T, S> transformation) {
        return pipe(transformation, SimpleEventStream::new);
    }

    default Subscription endIn(Sink<? super T> sink) {
        return subscribe(sink::push);
    }

    default Subscription branchTo(DataStream<? super T> eventStream) {
        return subscribe(eventStream::push);
    }

    default <S> Subscription connectTo(DataStream<? super S> eventStream, Function<T, S> function) {
        return subscribe(t -> eventStream.push(function.apply(t)));
    }

}
