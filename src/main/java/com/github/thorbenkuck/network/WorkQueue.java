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

    public static void append(Runnable runnable) {
        try {
            System.out.println("Putting new Task ..");
            tasks.put(runnable);
            System.out.println("New Task submitted!");
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
            System.out.println("Started ..");
            while (running) {
                try {
                    System.out.println("Taking next task ..");
                    Runnable take = tasks.take();
                    take.run();
                    System.out.println("Task done ..");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Stopped ..");
        }
    }

}
