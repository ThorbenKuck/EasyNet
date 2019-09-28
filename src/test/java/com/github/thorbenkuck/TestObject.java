package com.github.thorbenkuck;

import java.io.Serializable;

public class TestObject implements Serializable {

    public final String message;

    public TestObject(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "TestObject{" +
                "message=" + message +
                "}";
    }
}
