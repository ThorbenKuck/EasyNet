package com.github.thorbenkuck.network.server;

import com.github.thorbenkuck.network.connection.Connection;
import com.github.thorbenkuck.network.connection.ConnectionContext;
import com.github.thorbenkuck.network.stream.Subscriber;
import com.github.thorbenkuck.network.stream.Subscription;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public class ServerHandshakeSubscriber implements Subscriber<String> {

    private final Connection connection;
    private final Function<String, ConnectionContext> contextSupplier;
    private final CompletableFuture<String> future = new CompletableFuture<>();
    private final StringBuilder stringBuilder = new StringBuilder();
    private String id;

    public ServerHandshakeSubscriber(String id, Connection connection, Function<String, ConnectionContext> contextSupplier) {
        this.id = id;
        this.connection = connection;
        this.contextSupplier = contextSupplier;
    }

    private void exit(String message, String exitVal) {
        connection.systemInput().push(message);
        future.complete(exitVal);
    }

    public Future<String> future() {
        return future;
    }

    @Override
    public void accept(String message) throws Exception {
        stringBuilder.append(message).append(System.lineSeparator());
        if (message.toLowerCase().equals("ok")) {
            exit("ok", id);
        } else if (message.toLowerCase().startsWith("request")) {
            String requested = message.substring(8);
            if (requested.equals(id)) {
                connection.systemInput().push("id " + id);
            } else {
                ConnectionContext targetContext = contextSupplier.apply(requested);
                if (targetContext == null) {
                    exit("reject", stringBuilder.append("no target found").append(System.lineSeparator()).append("reject").toString());
                } else {
                    CompletableFuture<String> future = new CompletableFuture<>();
                    Subscription subscription = targetContext.systemOutput().subscribe(future::complete);

                    targetContext.systemInput().push("known " + id);
                    stringBuilder.append("Requesting ").append(id).append(" for knowledge .. ");
                    String result = future.get();
                    subscription.cancel();

                    if (result.toLowerCase().equals("ok")) {
                        stringBuilder.append("[OK]").append(System.lineSeparator());
                        id = requested;
                        connection.systemInput().push("id " + requested);
                    } else {
                        stringBuilder.append("[ERR]").append(System.lineSeparator());
                        System.err.println("!WARN! Target( " + targetContext.remoteAddress() + ") does NOT know origin (" + connection.remoteAddress() + ")! Initializing disconnect");
                        exit("reject", stringBuilder.append("reject").toString());
                    }
                }
            }
        } else {
            System.err.println("Unknown system message " + message);
            exit("reject", stringBuilder.append("unknown request ").append(message).append(System.lineSeparator()).append("reject").toString());
        }
    }
}
