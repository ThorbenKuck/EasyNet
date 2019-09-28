package com.github.thorbenkuck;

import com.github.thorbenkuck.network.server.ServerConnectionFactory;
import com.github.thorbenkuck.network.server.ServerContainer;
import com.github.thorbenkuck.network.stream.Subscription;
import com.github.thorbenkuck.network.utils.StopWatch;

public class ServerTest {

    public static void main(String[] args) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.prepareStep("Creating Builder");
        stopWatch.prepareStep("Starting");
        stopWatch.start();
        System.out.print("Building ... ");
        ServerConnectionFactory build = ServerConnectionFactory.builder()
                .blocking()
                .build();
        System.out.println("OK");
        stopWatch.step();
        System.out.print("Starting Server ... ");
        try (ServerContainer serverContainer = ServerContainer.open(9999, build)) {
            System.out.println("OK");

            stopWatch.step();
            stopWatch.stop();
            stopWatch.print();

            Subscription sendSubscription = serverContainer.output().subscribe(remoteMessage -> remoteMessage.context().write(remoteMessage.data()));
            serverContainer.accept();
            System.out.println("Server done. Closing");
            sendSubscription.cancel();
        }
    }
}
