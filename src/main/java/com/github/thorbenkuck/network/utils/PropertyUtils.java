package com.github.thorbenkuck.network.utils;

public class PropertyUtils {

    public static int amountOfWorkers() {
        String property = System.getProperty("easy.net.worker.amount", "8");

        return Integer.parseInt(property);
    }

    public static boolean exitOnError() {
        String property = System.getProperty("easy.net.implicit.error", "false");

        return Boolean.parseBoolean(property);
    }

    public static boolean daemonWorkerThreads() {
        String property = System.getProperty("easy.net.worker.daemon", "false");

        return Boolean.parseBoolean(property);
    }
}
