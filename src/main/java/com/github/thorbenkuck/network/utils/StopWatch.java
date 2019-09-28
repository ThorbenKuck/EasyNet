package com.github.thorbenkuck.network.utils;

import java.util.*;

public class StopWatch {

    private final Map<Integer, String> mapping = new HashMap<>();
    private long start;
    private List<Long> snaps = new ArrayList<>();
    private long elapsed;
    private int counter = 1;

    private long now() {
        return System.currentTimeMillis();
    }

    private long toMillis(long time) {
        return time;
    }

    public void prepareStep(String name) {
        mapping.put(counter++, name);
    }

    public void start() {
        start = now();
    }

    public synchronized void step() {
        snaps.add(now() - start);
    }

    public void stop() {
        long end = now();
        long start = this.start;
        this.start = -1;

        this.elapsed = end - start;
    }

    public void print() {
        int counter = 0;
        long last = 0;
        System.out.println("### StopWatch report start");
        long[] elapsedArray = new long[snaps.size()];
        for (long l : snaps) {
            String name = mapping.getOrDefault(++counter, String.valueOf(counter));
            long elapsed = toMillis(l - last);
            elapsedArray[counter - 1] = elapsed;
            last = l;
            System.out.println("Step \"" + name + "\" took " + elapsed + "ms (snap taken at " + toMillis(l) + ")");
        }
        System.out.println("End difference " + toMillis(elapsed - last));
        System.out.println("Total time elapsed: " + toMillis(elapsed));
        System.out.println("Average time per step: " + toMillis(elapsed / counter));
        Arrays.sort(elapsedArray);
        double median;
        if (elapsedArray.length % 2 == 0) {
            median = ((double) elapsedArray[elapsedArray.length / 2] + (double) elapsedArray[elapsedArray.length / 2 - 1]) / 2;
        } else {
            median = elapsedArray[elapsedArray.length / 2];
        }

        System.out.println("Median time per step: " + median);
        System.out.println("Minimum time per step: " + elapsedArray[0]);
        System.out.println("Maximum time per step: " + elapsedArray[elapsedArray.length - 1]);
        System.out.println("### StopWatch report end");
    }
}
