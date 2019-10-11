package com.github.thorbenkuck.network.stream;

import java.util.function.Consumer;

public interface SourceConsumer<T> extends Consumer<T> {

  void onCancel();

}
