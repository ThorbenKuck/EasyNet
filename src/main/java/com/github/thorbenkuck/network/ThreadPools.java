package com.github.thorbenkuck.network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadPools {

    private static final ThreadFactory DAEMON_THREAD_FACTORY = new DaemonThreadFactory();

    public static ExecutorService newDaemonThreadPool() {
        return Executors.newCachedThreadPool(new DaemonThreadFactory());
    }

    public static Thread runDaemonThread(Runnable runnable, String name) {
        Thread thread = DAEMON_THREAD_FACTORY.newThread(runnable);
        if (name != null) {
            thread.setName(name);
        }
        thread.start();
        return thread;
    }

    public static Thread newDaemonThread(Runnable runnable, String name) {
        Thread thread = DAEMON_THREAD_FACTORY.newThread(runnable);
        if (name != null) {
            thread.setName(name);
        }
        return thread;
    }

    private static final class DaemonThreadFactory implements ThreadFactory {

        private final String name;

        private DaemonThreadFactory(String name) {
            this.name = name;
        }

        private DaemonThreadFactory() {
            name = "WorkQueue Task";
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName(name);
            thread.setDaemon(true);
            return thread;
        }
    }
}
