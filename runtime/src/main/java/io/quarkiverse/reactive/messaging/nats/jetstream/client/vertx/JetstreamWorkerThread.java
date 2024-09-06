package io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx;

import java.util.concurrent.atomic.AtomicInteger;

class JetstreamWorkerThread extends Thread {

    private static final AtomicInteger threadCount = new AtomicInteger(0);

    JetstreamWorkerThread(Runnable task) {
        super(task, "nats-jetstream-worker-" + threadCount.incrementAndGet());
    }
}
