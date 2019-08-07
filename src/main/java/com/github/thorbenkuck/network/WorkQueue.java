package com.github.thorbenkuck.network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class WorkQueue {

    private static final LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    static {
        for(int i = 0 ; i < PropertyUtils.amountOfWorkers() ; i++) {
            addWorker();
        }
    }

    public static void shutdown() {
        threadPool.shutdownNow();
    }

    public static void append(Runnable runnable) {
        try {
            tasks.put(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void addWorker() {
        threadPool.execute(new Worker());
    }

    private static class Worker implements Runnable {

        private boolean running;

        @Override
        public void run() {
            running = true;
            while (running) {
                try {
                    Runnable take = tasks.take();
                    take.run();
                } catch (InterruptedException e) {
                    running = false;
                }
            }
        }
    }

}
