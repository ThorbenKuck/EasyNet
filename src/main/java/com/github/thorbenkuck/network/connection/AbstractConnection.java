package com.github.thorbenkuck.network.connection;

import com.github.thorbenkuck.network.stream.DataStream;
import com.github.thorbenkuck.network.stream.EventStream;
import com.github.thorbenkuck.network.stream.ManagedEventStream;

import java.io.IOException;
import java.util.function.Consumer;

public abstract class AbstractConnection implements Connection {

    protected final ManagedEventStream<byte[]> output = ManagedEventStream.sequential();
    protected final ManagedEventStream<byte[]> input = ManagedEventStream.sequential();
    protected final ManagedEventStream<String> systemInput = ManagedEventStream.sequential();
    protected final ManagedEventStream<String> systemOutput = ManagedEventStream.sequential();
    protected final Object protocolLock = new Object();
    private Consumer<Connection> onDisconnect;
    private Protocol protocol = new SizeFirstProtocol();

    protected void pipeInputStreams() {
        input.subscribe(this::silentWrite);
        systemInput.subscribe(s -> silentWrite(("sys " + s).getBytes()));
    }

    protected void write(byte[] bytes) throws IOException {
        writeToProtocol(bytes);
    }

    protected void rawWrite(byte[] bytes) throws IOException {
        getDataConnection().write(bytes);
        getDataConnection().flush();
    }

    protected void received(byte[] data) {
        if (data.length < 200) {
            String potential = new String(data);
            if (potential.toLowerCase().startsWith("sys")) {
                systemOutput.push(potential.substring(4));
            } else {
                output.push(data);
            }
        } else {
            output.push(data);
        }
    }

    protected void silentWrite(byte[] bytes) {
        try {
            writeToProtocol(bytes);
        } catch (IOException ignored) {
        }
    }

    protected void silentRawWrite(byte[] bytes) {
        try {
            rawWrite(bytes);
        } catch (IOException ignored) {
        }
    }

    protected void acceptError(Throwable throwable) {
        output.pushError(throwable);
    }

    protected synchronized void triggerDisconnectEvent() {
        if (onDisconnect != null) {
            onDisconnect.accept(this);
            onDisconnect = null;
        }
    }

    @Override
    public Protocol getProtocol() {
        synchronized (protocolLock) {
            return protocol;
        }
    }

    @Override
    public void setProtocol(Protocol protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("Protocol must not be null!");
        }
        synchronized (protocolLock) {
            this.protocol = protocol;
        }
    }

    @Override
    public byte[] readFromProtocol() throws IOException {
        return getProtocol().readNext(getDataConnection());
    }

    @Override
    public void writeToProtocol(byte[] data) throws IOException {
        getProtocol().write(data, getDataConnection());
    }

    @Override
    public final EventStream<byte[]> output() {
        return output;
    }

    @Override
    public final DataStream<byte[]> input() {
        return input;
    }

    @Override
    public final EventStream<String> systemOutput() {
        return systemOutput;
    }

    @Override
    public final DataStream<String> systemInput() {
        return systemInput;
    }

    @Override
    public final Consumer<Connection> getOnDisconnect() {
        return onDisconnect;
    }

    @Override
    public final void setOnDisconnect(Consumer<Connection> onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    public abstract DataConnection getDataConnection();

    @Override
    public void close() throws IOException {
        triggerDisconnectEvent();
        output.close();
        input.close();
    }

    protected void setOutputPaused(boolean to) {
        if (to) {
            output.pause();
        } else {
            output.unPause();
        }
    }

    @Override
    public final void pauseOutput() {
        setOutputPaused(true);
    }

    @Override
    public final void unpauseOutput() {
        setOutputPaused(false);
    }
}
