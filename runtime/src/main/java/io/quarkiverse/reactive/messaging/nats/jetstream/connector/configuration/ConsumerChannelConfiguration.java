package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Represents the configuration of a consumer channel. This interface extends
 * {@link ChannelConfiguration} and defines additional configurations specific
 * to a consumer, enabling properties related to processing messages from a
 * stream or datasource.
 */
public interface ConsumerChannelConfiguration extends ChannelConfiguration {

    /**
     * Retrieves the name of the consumer associated with the consumer channel configuration.
     *
     * @return the name of the consumer as a non-null String
     */
    @NonNull
    Optional<String> consumer();

    /**
     * Retrieves the specific payload type expected by the consumer channel.
     * The payload type indicates the class that represents the structure of the
     * messages the consumer expects to process. If no explicit type is configured,
     * an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the payload type as a {@code Class<?>}, or empty if unspecified.
     */
    @NonNull
    <T> Optional<Class<T>> payloadType();

    /**
     * Retrieves the batch size configuration for the consumer channel.
     * The batch size specifies the maximum number of messages that can
     * be processed in a single batch.
     *
     * @return the batch size as a non-null Integer
     */
    @NonNull
    Integer batchSize();

    /**
     * Retrieves the timeout duration for the consumer channel.
     * The timeout specifies the maximum amount of time the consumer
     * will wait for messages or a response during processing.
     *
     * @return the timeout duration as a non-null {@code Duration}
     */
    @NonNull
    Duration timeout();

    /**
     * Retrieves the name of the consumer associated with the current consumer channel configuration.
     * If the consumer name is not configured, this method will throw an {@code IllegalArgumentException}.
     *
     * @return the name of the consumer as a non-null String
     * @throws IllegalArgumentException if the consumer name is missing
     */
    default @NonNull String getConsumer() {
        return consumer()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing consumer for channel: %s", name())));
    }
}
