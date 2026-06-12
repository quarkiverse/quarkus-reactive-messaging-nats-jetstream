package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import java.util.concurrent.atomic.AtomicInteger;

public class SubscriptionWorkerThread extends Thread {
    private static final AtomicInteger threadCount = new AtomicInteger(0);

    public SubscriptionWorkerThread(Runnable task) {
        super(task, "subscription-worker-" + threadCount.incrementAndGet());
    }
}
