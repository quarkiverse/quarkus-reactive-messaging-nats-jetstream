package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.smallrye.config.WithDefault;

/**
 * The configuration of the thread pool owned by the client of a datasource. The blocking calls of publishing a
 * message (sending it and waiting for the JetStream acknowledgement) run on this pool.
 */
public interface ThreadPoolConfiguration {

    /**
     * Whether every task runs on a new virtual thread instead of a pooled platform thread. When enabled, the core
     * size, max size, queue size and keep alive are ignored. Requires Java 21 or later; on older Java versions a
     * warning is logged and the pooled platform threads are used.
     */
    @WithDefault("false")
    boolean virtualThreads();

    /**
     * The number of threads kept in the pool, even when they are idle.
     */
    @WithDefault("1")
    int coreSize();

    /**
     * The maximum number of threads in the pool. Defaults to 8 times the number of available processors.
     */
    @NonNull
    Optional<Integer> maxSize();

    /**
     * The maximum number of tasks waiting for a thread when all threads are busy. Unbounded when not set. A publish
     * submitted when the queue is full is rejected, and the message is not acknowledged.
     */
    @NonNull
    Optional<Integer> queueSize();

    /**
     * How long a thread above the core size may stay idle before it is stopped.
     */
    @NonNull
    @WithDefault("30s")
    Duration keepAlive();

    /**
     * How long closing the client waits for running tasks to complete before interrupting them.
     */
    @NonNull
    @WithDefault("10s")
    Duration shutdownTimeout();
}
