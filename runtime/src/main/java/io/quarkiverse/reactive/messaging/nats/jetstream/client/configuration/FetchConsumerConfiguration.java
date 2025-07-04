package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.Optional;

import io.smallrye.config.WithDefault;

public interface FetchConsumerConfiguration<T> extends ConsumerConfiguration<T> {

    @Override
    default ConsumerType type() {
        return ConsumerType.Fetch;
    }

    /**
     * The timeout for fetching messages.
     */
    Optional<Duration> timeout();

    /**
     * The maximum number of messages to fetch in a single batch.
     */
    @WithDefault("1")
    Integer batchSize();
}
