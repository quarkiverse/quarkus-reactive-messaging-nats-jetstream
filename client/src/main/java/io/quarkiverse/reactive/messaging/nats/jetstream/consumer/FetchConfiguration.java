package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Represents the configuration settings for fetching messages from a messaging system.
 * This interface defines methods to customize and control the behavior of message fetching.
 */
public interface FetchConfiguration {

    /**
     * The timeout for fetching messages.
     */
    @NonNull
    Optional<Duration> timeout();

    /**
     * The maximum number of messages to fetch in a single batch.
     */
    @NonNull
    Integer batchSize();
}
