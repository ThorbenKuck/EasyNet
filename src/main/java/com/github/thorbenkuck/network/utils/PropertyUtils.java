package com.github.thorbenkuck.network.utils;

public class PropertyUtils {

    public static int amountOfWorkers() {
        String property = System.getProperty("easy.net.worker.amount", "8");

        return Integer.parseInt(property);
    }
}
