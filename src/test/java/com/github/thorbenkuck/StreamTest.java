package com.github.thorbenkuck;

import com.github.thorbenkuck.network.exceptions.EmptySubscriberListException;
import com.github.thorbenkuck.network.stream.ManagedEventStream;
import com.github.thorbenkuck.network.stream.Sink;
import com.github.thorbenkuck.network.stream.Source;
import com.github.thorbenkuck.network.stream.Subscriber;
import com.github.thorbenkuck.network.stream.Subscription;

public class StreamTest {

  public static void main(String[] args) throws InterruptedException {
    second();
  }

  private static void second() throws InterruptedException {
    Source<TestObject2> source = Source.create();
    Sink<TestObject> sink = Sink.of(System.out::println);
    ManagedEventStream<TestObject> stream = ManagedEventStream.parallel(source);
    ManagedEventStream<TestObject> branch1 = ManagedEventStream.sequential();
    ManagedEventStream<TestObject> branch2 = ManagedEventStream.sequential();

    stream.branchTo(branch1);
    stream.branchTo(branch2);

    branch1.subscribe(t -> {
      System.out.println("[Branch1, " + Thread.currentThread() + "] passed");
    });
    branch2.subscribe(t -> {
      System.out.println("[Branch2, " + Thread.currentThread() + "] passed");
      Thread.sleep(1000);
    });

    sink.attachTo(branch1);
    sink.attachTo(branch2);

    source.push(new TestObject2());
  }

  private static void first() {
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
      super("message");
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
