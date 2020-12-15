package com.github.thorbenkuck;

import com.github.thorbenkuck.network.client.ClientContainer;
import com.github.thorbenkuck.network.stream.Subscription;
import com.github.thorbenkuck.network.utils.StopWatch;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ClientTest {

    private static final String ADDRESSES = "localhost";
    private static final int PORT = 9999;

    public static void main(String[] args) {
        System.out.print("Starting and Stopping 100 Clients ... ");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < 100; i++) {
            run();
            stopWatch.step();
		}

        stopWatch.stop();
        System.out.println("OK");
        System.out.println("Printing time");
        stopWatch.print();
    }

    private static void run() {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        try (ClientContainer main = ClientContainer.builder()
                .blocking()
                .build(ADDRESSES, PORT)) {
            Subscription subscribeOne = main.output().subscribe(o -> countDownLatch.countDown());
            main.listen();

            ClientContainer sub = main.createSub();
            Subscription subscribeTwo = sub.output()
                    .subscribe(o -> countDownLatch.countDown());

            main.input().push(new TestObject("Client1"));
            sub.input().push(new TestObject("Client2"));

            countDownLatch.await();

            subscribeOne.cancel();
            subscribeTwo.cancel();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
