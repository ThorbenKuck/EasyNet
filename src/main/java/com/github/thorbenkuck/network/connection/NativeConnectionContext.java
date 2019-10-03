package com.github.thorbenkuck.network.connection;

import com.github.thorbenkuck.network.stream.DataStream;
import com.github.thorbenkuck.network.stream.EventStream;

import java.net.SocketAddress;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class NativeConnectionContext implements ConnectionContext {

    private final Connection connection;
    private final Supplier<String> idSupplier;
    private final Function<Object, byte[]> convert;
    private final EventHandler<ConnectionContext> disconnected = new EventHandler<>();

    NativeConnectionContext(Connection connection, Supplier<String> idSupplier, Function<Object, byte[]> convert) {
        this.connection = connection;
        connection.setOnDisconnect(c -> disconnected.propagate(this));
        this.idSupplier = idSupplier;
        this.convert = convert;
    }

    @Override
    public void write(Object o) {
        inputStream().push(convert.apply(o));
    }

    @Override
    public DataStream<byte[]> inputStream() {
        return connection.input();
    }

    @Override
    public EventStream<byte[]> outputStream() {
        return connection.output();
    }

    @Override
    public DataStream<String> systemInput() {
        return connection.systemInput();
    }

    @Override
    public EventStream<String> systemOutput() {
        return connection.systemOutput();
    }

    @Override
    public String getIdentifier() {
        return idSupplier.get();
    }

    @Override
    public SocketAddress remoteAddress() {
        return connection.remoteAddress();
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public void onDisconnect(Consumer<ConnectionContext> consumer) {
        disconnected.addHandler(consumer);
    }

    @Override
    public void onDisconnect(Runnable runnable) {
        disconnected.addHandler(runnable);
    }
}
