package com.github.thorbenkuck.network.stream;

public interface Transformation<F, T> {

    T apply(F f) throws Exception;

}
