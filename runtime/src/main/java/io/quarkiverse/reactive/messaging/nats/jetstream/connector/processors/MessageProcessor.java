package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors;

import org.jspecify.annotations.NonNull;

/**
 * Represents a processor responsible for handling messages within a messaging system.
 * Implementations of this interface can define specific behavior for consuming,
 * publishing, and managing messages across streams and channels.
 * This interface provides methods to interact with the processor and manage its operations.
 */
public interface MessageProcessor {

    /**
     * Returns the name of the channel associated with the message processor.
     * This method identifies the specific channel used by the messaging system for communication
     * and is essential for tracking or debugging operations related to the processor.
     *
     * @return a non-null string representing the name of the channel.
     */
    @NonNull
    String channel();

    /**
     * Retrieves the name of the stream associated with the message processor.
     * This method provides the specific identifier for the stream being utilized by the message
     * processor, which can be used for monitoring or managing message streams in the system.
     *
     * @return a non-null string representing the name of the stream.
     */
    @NonNull
    String stream();

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
