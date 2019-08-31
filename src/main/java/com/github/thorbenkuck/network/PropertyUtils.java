package com.github.thorbenkuck.network;

public class PropertyUtils {

    public static int amountOfWorkers() {
        String property = System.getProperty("easy.net.worker.amount", "4");

        return Integer.parseInt(property);
    }

}
