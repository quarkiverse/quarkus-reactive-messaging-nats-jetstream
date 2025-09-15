package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerWorkerThread extends Thread {

    private static final AtomicInteger threadCount = new AtomicInteger(0);

    public ConsumerWorkerThread(Runnable task) {
        super(task, "consumer-worker-" + threadCount.incrementAndGet());
    }
}
