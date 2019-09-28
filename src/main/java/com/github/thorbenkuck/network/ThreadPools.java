package com.github.thorbenkuck.network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadPools {

    private static final ThreadFactory NON_DAEMON_THREAD_FACTORY = new DaemonThreadFactory();

    public static ExecutorService newDaemonThreadPool() {
        return Executors.newCachedThreadPool(new DaemonThreadFactory("WorkQueue Task"));
    }

    public static Thread runDaemonThread(Runnable runnable, String name) {
        Thread thread = NON_DAEMON_THREAD_FACTORY.newThread(runnable);
        thread.setName(name);
        thread.start();
        return thread;
    }

    private static final class DaemonThreadFactory implements ThreadFactory {

        private final String name;

        private DaemonThreadFactory(String name) {
            this.name = name;
        }

        private DaemonThreadFactory() {
            name = null;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            if (name != null) {
                thread.setName(name);
                thread.setName("WorkQueue Task");
            }
            thread.setDaemon(true);
            return thread;
        }
    }
}
