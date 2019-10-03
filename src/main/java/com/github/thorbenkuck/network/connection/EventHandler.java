package com.github.thorbenkuck.network.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventHandler<T> {

    private final List<Consumer<T>> consumers = new ArrayList<>();

    public void addHandler(Consumer<T> consumer) {
        consumers.add(consumer);
    }

    public void addHandler(Runnable runnable) {
        consumers.add(new RunnableWrapper<>(runnable));
    }

    public void removeHandler(Consumer<T> consumer) {
        consumers.remove(consumer);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public void removeHandler(Runnable consumer) {
        consumers.remove(new RunnableWrapper<>(consumer));
    }

    public void propagate(T t) {
        ArrayList<Consumer<T>> copy = new ArrayList<>(this.consumers);
        copy.forEach(c -> c.accept(t));
    }

    private static class RunnableWrapper<T> implements Consumer<T> {

        private final Runnable root;

        private RunnableWrapper(Runnable root) {
            this.root = root;
        }

        @Override
        public void accept(T t) {
            root.run();
        }

        @Override
        public boolean equals(Object o) {
            return root.equals(o);
        }

        @Override
        public String toString() {
            return root.toString();
        }

        @Override
        public int hashCode() {
            return root.hashCode();
        }
    }

}
