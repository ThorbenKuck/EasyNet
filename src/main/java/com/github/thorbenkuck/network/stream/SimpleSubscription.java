package com.github.thorbenkuck.network.stream;

import com.github.thorbenkuck.network.exceptions.SubscriptionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class SimpleSubscription<T> implements NotifiableSubscription<T> {

    private final List<Throwable> throwableBuffer = new ArrayList<>();
    private final Object mutexLock = new Object();
    private final Object cancelLock = new Object();
    private Subscriber<T> subscriber;
    private boolean bufferErrors = true;
    private SubscriptionReference<NotifiableSubscription<T>> subscriptionReference;
    private Runnable onCancel;
    private ExceptionalConsumer<Throwable> errorConsumer;

    SimpleSubscription(Subscriber<T> subscriber, SubscriptionReference<NotifiableSubscription<T>> subscriptionReference) {
        this.subscriber = subscriber;
        this.subscriptionReference = subscriptionReference;
    }

    private void runEncapsulated(ExceptionalRunnable exceptionalRunnable) {
        try {
            exceptionalRunnable.run();
        } catch (Throwable throwable) {
            if (throwable instanceof SubscriptionException) {
                throw (SubscriptionException) throwable;
            }
            throw new SubscriptionException(throwable);
        }
    }

    private void addThrowable(Throwable throwable) {
        try {
            subscriber.onError(throwable);
        } catch (Throwable e) {
            if (errorConsumer != null) {
                runEncapsulated(() -> errorConsumer.accept(e));
            } else {
                if (bufferErrors) {
                    throwableBuffer.add(throwable);
                } else {
                    runEncapsulated(() -> {
                        throw throwable;
                    });
                }
            }
        }
    }

    @Override
    public NotifiableSubscription<T> onError(ExceptionalConsumer<Throwable> consumer) {
        this.errorConsumer = Objects.requireNonNull(consumer);

        return this;
    }

    @Override
    public void notify(T t) {
        synchronized (mutexLock) {
            if (!isCanceled()) {
                try {
                    subscriber.accept(t);
                } catch (Throwable throwable) {
                    addThrowable(throwable);
                }
            }
        }
    }

    @Override
    public void notify(Throwable throwable) {
        addThrowable(throwable);
    }

    @Override
    public boolean isCanceled() {
        return subscriber == null;
    }

    @Override
    public void cancel() {
        if (isCanceled()) {
            return;
        }
        synchronized (mutexLock) {
            if (subscriber != null) {
                subscriber.onCancel();
                subscriber = null;
            }

            if (subscriptionReference != null) {
                subscriptionReference.remove(this);
                subscriptionReference = null;
            }

            synchronized (cancelLock) {
                if (onCancel != null) {
                    try {
                        onCancel.run();
                    } catch (Throwable t) {
                        addThrowable(t);
                    }
                    onCancel = null;
                }
            }
        }
    }

    @Override
    public void setOnCancel(Runnable runnable) {
        synchronized (cancelLock) {
            this.onCancel = runnable;
        }
    }

    @Override
    public List<Throwable> drainEncountered() {
        List<Throwable> toReturn = new ArrayList<>(throwableBuffer);
        throwableBuffer.clear();
        return toReturn;
    }

    @Override
    public boolean hasEncounteredErrors() {
        return !throwableBuffer.isEmpty();
    }

    @Override
    public void preventErrorBuffer() {
        bufferErrors = false;
    }

    @Override
    public String toString() {
        return "SimpleSubscription{" + "subscriber=" + subscriber +
                ", throwableBuffer=" + throwableBuffer +
                ", onCancel=" + onCancel +
                '}';
    }
}
