package com.github.thorbenkuck.network.stream;

import java.util.List;

public class DelegatingEventStream<T> implements ManagedEventStream<T> {

    private final ManagedEventStream<T> delegate;

    public DelegatingEventStream(ManagedEventStream<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void clearSubscribers() {
        delegate.clearSubscribers();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void pause() {
        delegate.pause();
    }

    @Override
    public void pushError(Throwable throwable) {
        delegate.pushError(throwable);
    }

    @Override
    public void unPause() {
        delegate.unPause();
    }

    @Override
    public boolean isPaused() {
        return delegate.isPaused();
    }

    @Override
    public boolean isDisabled() {
        return delegate.isDisabled();
    }

    @Override
    public void setDisabled(boolean disabled) {
        delegate.setDisabled(disabled);
    }

    @Override
    public void push(T o) {
        delegate.push(o);
    }

    @Override
    public Subscription subscribe(Subscriber<T> subscriber) {
        return delegate.subscribe(subscriber);
    }

    @Override
    public List<NotifiableSubscription<T>> getSubscriptions() {
        return delegate.getSubscriptions();
    }
}
