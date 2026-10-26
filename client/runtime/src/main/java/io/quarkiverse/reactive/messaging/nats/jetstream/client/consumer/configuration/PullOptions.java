package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Interface representing options for configuring pull-based consumption
 * from a JetStream consumer in NATS.
 */
public interface PullOptions {

    /**
     * The number of pulls that can be outstanding on a pull consumer
     *
     * @return the number of pulls that can be outstanding on a pull consumer
     */
    @NonNull
    Optional<Long> maxWaiting();

    /**
     * The max amount of expire time for the server to allow on pull requests.
     *
     * @return the max amount of expire time for the server to allow on pull requests.
     */
    @NonNull
    Optional<Duration> maxExpires();

    /**
     * The maximum batch size a single pull request can make. When set with MaxRequestMaxBytes;
     * the batch size will be constrained by whichever limit is hit first.
     *
     * @return The maximum batch size
     */
    @NonNull
    Optional<Long> maxBatch();

    /**
     * The maximum total bytes that can be requested in a given batch. When set with MaxRequestBatch;
     * the batch size will be constrained by whichever limit is hit first.
     *
     * @return The maximum total bytes that can be requested in a given batch
     */
    @NonNull
    Optional<Long> maxBytes();
}
