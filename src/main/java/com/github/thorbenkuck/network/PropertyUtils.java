package com.github.thorbenkuck.network;

public class PropertyUtils {

    public static int amountOfWorkers() {
        String property = System.getProperty("easy.net.worker.amount");

        if(property == null) {
            return 4;
        } else {
            return Integer.parseInt(property);
        }
    }

}
