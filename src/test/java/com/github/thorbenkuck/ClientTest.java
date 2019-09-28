package com.github.thorbenkuck;

import com.github.thorbenkuck.network.client.ClientContainer;
import com.github.thorbenkuck.network.utils.StopWatch;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTest {

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static final String ADDRESSES = "localhost";
    private static final int PORT = 9999;

    public static void main(String[] args) throws Exception {
        System.out.print("Starting and Stopping 100 Clients ... ");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            threadPool.submit(() -> {
                try {
                    run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stopWatch.step();
                latch.countDown();
            });
		}

        latch.await();
        stopWatch.stop();
        System.out.println("OK");
        System.out.println("Printing time");
        stopWatch.print();
    }

    private static void run() throws IOException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        try (ClientContainer main = ClientContainer.builder()
                .blocking()
                .build(ADDRESSES, PORT)) {
            main.output().subscribe(o -> countDownLatch.countDown());
            main.listen();

            ClientContainer sub = main.createSub();
            sub.output().subscribe(o -> countDownLatch.countDown());

            main.input().push(new TestObject("Client1"));
            sub.input().push(new TestObject("Client2"));

            countDownLatch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
