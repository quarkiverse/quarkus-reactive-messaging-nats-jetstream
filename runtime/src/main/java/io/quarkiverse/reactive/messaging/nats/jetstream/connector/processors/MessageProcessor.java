package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ChannelConfiguration;

/**
 * Represents a processor responsible for handling messages within a messaging system.
 * Implementations of this interface can define specific behavior for consuming,
 * publishing, and managing messages across streams and channels.
 * This interface provides methods to interact with the processor and manage its operations.
 */
public interface MessageProcessor {

    /**
     * Retrieves the channel configuration associated with the message processor.
     * This configuration provides details about the channel, including its name,
     * associated stream, retry backoff settings, and the underlying datasource.
     *
     * @return a non-null {@code ChannelConfiguration} object representing the channel configuration.
     */
    @NonNull
    ChannelConfiguration channelConfiguration();

    /**
     * Retrieves the health status of the message processor.
     * This method provides detailed information about the processor's health,
     * including whether it is operating correctly and a descriptive message.
     *
     * @return a non-null {@code Health} object containing the health status and an associated message.
     */
    @NonNull
    Health health();

    /**
     * Stops the message processor.
     * This method is invoked to signal the processor to cease operations,
     * typically as part of a shutdown procedure. Implementations should ensure
     * all resources are released and any ongoing tasks are either completed or safely halted.
     */
    void stop();

}
