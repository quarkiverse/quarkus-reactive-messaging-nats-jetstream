package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.util.concurrent.atomic.AtomicInteger;

public class JetstreamWorkerThread extends Thread {

    private static final AtomicInteger threadCount = new AtomicInteger(0);

    public JetstreamWorkerThread(Runnable task) {
        super(task, "nats-jetstream-worker-" + threadCount.incrementAndGet());
    }
}
