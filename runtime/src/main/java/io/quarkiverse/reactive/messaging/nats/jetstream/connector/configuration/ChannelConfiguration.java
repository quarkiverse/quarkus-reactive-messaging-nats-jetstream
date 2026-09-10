package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Represents the configuration for a channel, defining key properties that
 * are used to interact with a data stream or datasource. A channel configuration
 * represents the common settings required for both producers and consumers
 * communicating via a streaming platform or messaging system.
 */
public interface ChannelConfiguration {

    /**
     * Retrieves the name of the channel configuration.
     *
     * @return the name of the channel as a non-null String
     */
    @NonNull
    String name();

    /**
     * Retrieves the name of the stream associated with the channel configuration.
     *
     * @return the name of the stream as a non-null String
     */
    @NonNull
    String stream();

    /**
     * Retrieves the retry backoff duration for the channel configuration, if specified.
     * The retry backoff duration indicates the time interval to wait before retrying
     * an operation after a failure.
     *
     * @return the retry backoff duration as a {@code Duration}
     */
    @NonNull
    Optional<Duration> retryBackoff();

    /**
     * Retrieves the name of the datasource associated with the channel configuration.
     *
     * @return the name of the datasource as a non-null String
     */
    @NonNull
    String datasource();

    /**
     * Retrieves the retry backoff duration configured for the channel.
     * The retry backoff duration specifies the time interval to wait before retrying
     * an operation after a failure. If no retry backoff is defined, this method
     * throws an {@code IllegalArgumentException}.
     *
     * @return the retry backoff duration as a non-null {@code Duration}
     * @throws IllegalArgumentException if the retry backoff duration is not defined
     */
    default @NonNull Duration getRetryBackoff() {
        return retryBackoff().orElse(Duration.ofMillis(10000));
    }
}
