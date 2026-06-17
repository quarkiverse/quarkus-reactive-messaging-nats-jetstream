package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import java.time.Duration;
import java.util.Optional;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

/**
 * Represents the configuration settings for fetching messages from a messaging system.
 * This interface defines methods to customize and control the behavior of message fetching.
 */
@Builder
public record FetchConfiguration(@NonNull Optional<Duration> timeout, @NonNull Integer batchSize) {

    /**
     * The timeout for fetching messages.
     */
    @Override
    @NonNull
    public Optional<Duration> timeout() {
        return timeout;
    }

    /**
     * The maximum number of messages to fetch in a single batch.
     */
    @Override
    @NonNull
    public Integer batchSize() {
        return batchSize;
    }
}
