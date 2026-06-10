package io.quarkiverse.reactive.messaging.nats.client.context;

import java.util.concurrent.atomic.AtomicInteger;

public class ContextWorkerThread extends Thread {
    private static final AtomicInteger threadCount = new AtomicInteger(0);

    public ContextWorkerThread(Runnable task) {
        super(task, "context-worker-" + threadCount.incrementAndGet());
    }
}
