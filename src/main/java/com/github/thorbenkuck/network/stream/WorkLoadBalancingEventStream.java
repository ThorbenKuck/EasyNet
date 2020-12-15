package com.github.thorbenkuck.network.stream;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkLoadBalancingEventStream<T> extends AbstractEventStream<T> {

    private final boolean async;
    private final ExecutorService executorService;
    private int pointer = 0;

    public WorkLoadBalancingEventStream(boolean async) {
        this.async = async;
        // This smells. I know. It is really a bad baaad smell.
        // This is done for memory footprint purposes. If this
        // event stream is not supposed to use threads, we do not
        // want to litter the memory with this.
        // I am sooooo so sorry that you have to see this..
        if (async) {
            executorService = Executors.newCachedThreadPool();
        } else {
            executorService = null;
        }
    }

    private int calculateNextPointer(int size) {
        final int potentialNext = ++pointer;
        if (potentialNext >= size) {
            return 0;
        }

        return potentialNext;
    }

    private void dispatch(T t, NotifiableSubscription<T> subscription) {
        if (async) {
            executorService.execute(() -> subscription.notify(t));
        } else {
            subscription.notify(t);
        }
    }

    @Override
    protected void dispatch(List<NotifiableSubscription<T>> notifiableSubscriptions, T t) {
        final int next = calculateNextPointer(notifiableSubscriptions.size());
        final NotifiableSubscription<T> subscription = notifiableSubscriptions.get(next);
        dispatch(t, subscription);
        pointer = next;
    }

    @Override
    public void close() {
        super.close();
        if (async) {
            executorService.shutdown();
        }
    }
}
