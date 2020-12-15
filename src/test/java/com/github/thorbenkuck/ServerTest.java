package com.github.thorbenkuck;

import com.github.thorbenkuck.network.RemoteMessage;
import com.github.thorbenkuck.network.connection.ConnectionContext;
import com.github.thorbenkuck.network.server.ServerConnectionFactory;
import com.github.thorbenkuck.network.server.ServerContainer;
import com.github.thorbenkuck.network.stream.Subscription;

public class ServerTest {

    public static void main(String[] args) throws Exception {
        ServerConnectionFactory build = ServerConnectionFactory.builder()
                .blocking()
                .build();

        try (ServerContainer serverContainer = ServerContainer.open(9999, build)) {
            Subscription sendSubscription = serverContainer.output().subscribe(ServerTest::handle);
            serverContainer.acceptAll();
            sendSubscription.cancel();
        }
    }

    private static void handle(RemoteMessage remoteMessage) {
        ConnectionContext context = remoteMessage.context();

        context.write(remoteMessage.data());
    }
}
