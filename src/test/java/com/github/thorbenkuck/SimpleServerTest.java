package com.github.thorbenkuck;

import com.github.thorbenkuck.network.server.ServerContainer;

import java.io.IOException;

public class SimpleServerTest {

    public static void main(String[] args) {
        try (ServerContainer serverContainer = ServerContainer.open(6767)) {
            serverContainer.output().subscribe(remoteMessage -> remoteMessage.context().write(remoteMessage.data()));
            serverContainer.output().subscribe(remoteMessage -> System.out.println(remoteMessage.data()));
            serverContainer.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
