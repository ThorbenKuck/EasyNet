package com.github.thorbenkuck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thorbenkuck.network.exceptions.EmptySubscriberListException;
import com.github.thorbenkuck.network.stream.*;

public class StreamTest {

    public static void main(String[] args) throws Exception {
        pipeTest();
        serializingPipeTest();
        workBalanceTest();
        branchTest();
    }

    private static void serializingPipeTest() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ManagedEventStream<byte[]> stream = ManagedEventStream.sequential();
        stream.pipe(t -> objectMapper.readValue(t, TestObject.class))
                .subscribe(System.out::println);

        stream.push(objectMapper.writeValueAsBytes(new TestObject("Should be serialized")));
    }

    private static void pipeTest() {
        ManagedEventStream<TestObject> stream = ManagedEventStream.sequential();
        stream.pipe(t -> new TestObject2())
                .subscribe(System.out::println);

        stream.push(new TestObject("I am the start"));
    }

    private static void branchTest() {
        Source<TestObject2> source = Source.create();
        Sink<TestObject> sink = Sink.of(System.out::println);
        ManagedEventStream<TestObject> stream = ManagedEventStream.parallel(source);
        ManagedEventStream<TestObject> branch1 = ManagedEventStream.sequential();
        ManagedEventStream<TestObject> branch2 = ManagedEventStream.sequential();

        stream.branchTo(branch1);
        stream.branchTo(branch2);

        branch1.subscribe(t -> System.out.println("[Branch1, " + Thread.currentThread() + "] passed"));
        branch2.subscribe(t -> {
            System.out.println("[Branch2, " + Thread.currentThread() + "] passed");
            Thread.sleep(1000);
        });

        sink.attachTo(branch1);
        sink.attachTo(branch2);

        source.push(new TestObject2());
    }

    private static void workBalanceTest() {
        ManagedEventStream<TestObject> eventStream = ManagedEventStream.balanced(true);
        eventStream.subscribe(t -> System.out.println("[1]: " + t));
        eventStream.subscribe(t -> System.out.println("[2]: " + t));
        eventStream.subscribe(t -> System.out.println("[3]: " + t));
        eventStream.subscribe(t -> System.out.println("[4]: " + t));

        for (int i = 0; i < 100; i++) {
            System.out.println("Pushing ...");
            eventStream.push(new TestObject2());
        }

        eventStream.close();
    }

    private static void first() {
        strictTest();
        System.out.println();
        System.out.println();
        System.out.println();
        connectTest();
    }

    public static void connectTest() {
        ManagedEventStream<String> source = ManagedEventStream.sequential();
        ManagedEventStream<Integer> sink = ManagedEventStream.sequential();

        Subscription connection = source.connectTo(sink, Integer::parseInt);
        Subscription subscribe2 = source.subscribe(s -> System.out.println("[SOURCE]: " + s));
        Subscription subscribe1 = sink.subscribe(s -> System.out.println("[SINK]: " + s));

        source.push("1234");

        connection.cancel();

        source.push("12345");

        subscribe1.cancel();
        subscribe2.cancel();
    }

    public static void strictTest() {
        ManagedEventStream<TestObject> stream = ManagedEventStream.strict();
        Subscription subscription = stream.subscribe(new CustomSubscriber());

        stream.push(new TestObject("message"));
        stream.pushError(new NullPointerException("This is a test Exception"));
        stream.push(new TestObject2());
        subscription.cancel();

        try {
            stream.push(new TestObject("message"));
        } catch (EmptySubscriberListException e) {
            e.printStackTrace(System.out);
        }
        System.out.println(subscription.isCanceled());
    }

    private static final class TestObject2 extends TestObject {
        private TestObject2() {
            super("TestObject2");
        }
    }

    private static final class CustomSubscriber implements Subscriber<TestObject> {

        @Override
        public void accept(TestObject testObject) {
            System.out.println(testObject);
        }

        @Override
        public void onCancel() {
            System.out.println("[CustomSubscriber] canceled");
        }

        @Override
        public void onSubscribe() {
            System.out.println("[CustomSubscriber] just subscribed");
        }

        @Override
        public void onError(Throwable throwable) {
            System.out.println("[CustomSubscriber] " + throwable.getMessage());
        }
    }

}
