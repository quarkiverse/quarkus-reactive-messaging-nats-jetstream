package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.time.ZonedDateTime;

import org.jspecify.annotations.NonNull;

import io.nats.client.impl.NatsJetStreamMetaData;

/**
 * Interface representing metadata information associated with a message.
 * This metadata is related to the delivery, stream, consumer, sequence numbers,
 * and timestamp of the message.
 */
public interface MessageMetadata extends Metadata {

    /**
     * The number of times this message has been delivered
     *
     * @return the number of times this message has been delivered
     */
    int deliveredCount();

    /**
     * Retrieves the name of the stream associated with this message metadata.
     *
     * @return the name of the stream, never null.
     */
    @NonNull
    String stream();

    /**
     * The consumer that generated this message
     *
     * @return the name of the consumer, never null.
     */
    @NonNull
    String consumer();

    /**
     * The stream sequence number of the message
     *
     * @return the stream sequence number of the message
     */
    long streamSequence();

    /**
     * The consumer sequence number of this message
     *
     * @return the consumer sequence number of this message
     */
    long consumerSequence();

    /**
     * The timestamp of the message
     *
     * @return the timestamp of the message
     */
    @NonNull
    ZonedDateTime timestamp();

    static @NonNull MessageMetadata of(@NonNull NatsJetStreamMetaData metaData) {
        return MessageMetadataImpl.builder()
                .consumer(metaData.getConsumer())
                .deliveredCount((int) metaData.deliveredCount())
                .streamSequence(metaData.streamSequence())
                .consumerSequence(metaData.consumerSequence())
                .timestamp(metaData.timestamp())
                .stream(metaData.getStream())
                .build();
    }
}
