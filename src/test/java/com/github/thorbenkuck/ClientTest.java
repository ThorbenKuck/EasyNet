package com.github.thorbenkuck;

import com.github.thorbenkuck.network.client.ClientContainer;

import java.io.IOException;
import java.util.Set;

public class ClientTest {

	private static void print(Object o) {
		System.out.println(o);
	}

	public static void main(String[] args) throws Exception {
//		for(int i = 0 ; i < 100 ; i++) {
		run();
//		}
	}

	private synchronized static void printThreads() {
		int threadCount = 0;
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		System.out.println("#################");
		for (Thread t : threadSet) {
			if (t.getThreadGroup() == Thread.currentThread().getThreadGroup()) {
				System.out.println("# Thread :" + t + ":" + "state:" + t.getState());
				++threadCount;
			}
		}
		System.out.println("# Count, started by Main thread:" + threadCount);
		System.out.println("#################");

	}

	private static void run() throws IOException {
		System.out.println("Starting Client1 .. ");
		ClientContainer main = ClientContainer.builder()
				.nonBlocking()
				.build("localhost", 9999);

		main.output().subscribe(o -> print("[Client1]: " + o));
		main.onDisconnect(connection -> System.out.println("Client1 Disconnected"));
		main.listen();
		System.out.println("[OK] Client1");

		System.out.println("Starting Client2 .. ");
		ClientContainer sub = main.createSub(container -> {
			container.output().subscribe(o -> print("[Client2]: " + o));
			container.onDisconnect(connection -> System.out.println("Client2 Disconnected"));
		});
		System.out.println("[OK] Client2");

		printThreads();

		main.input().push(new TestObject());
		sub.input().push(new TestObject());

		main.closeSilently();

		Thread closer = new Thread(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("main=" + main);
			System.out.println("sub=" + sub);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			printThreads();
		});
		closer.setName("Closer");
		closer.start();
	}

}
