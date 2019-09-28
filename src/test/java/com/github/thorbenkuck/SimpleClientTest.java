package com.github.thorbenkuck;

import com.github.thorbenkuck.network.RemoteMessage;
import com.github.thorbenkuck.network.client.ClientContainer;

import java.io.IOException;

public class SimpleClientTest {

    public static void main(String[] args) {
        try (ClientContainer clientContainer = ClientContainer.open("localhost", 6767)) {
            clientContainer.listen();
            clientContainer.output().subscribe(SimpleClientTest::print);
            clientContainer.input().push(new TestObject("Hallo!"));
            Thread.sleep(500);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void print(RemoteMessage remoteMessage) {
        System.out.println(remoteMessage.data());
    }
}
