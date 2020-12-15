package com.github.thorbenkuck;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.Serializable;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
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
