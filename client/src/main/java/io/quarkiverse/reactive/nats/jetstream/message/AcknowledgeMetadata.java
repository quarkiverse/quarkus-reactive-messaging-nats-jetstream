package io.quarkiverse.reactive.nats.jetstream.message;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.nats.client.api.PublishAck;

public interface AcknowledgeMetadata extends Metadata {

    static AcknowledgeMetadata of(PublishAck ack) {
        return AcknowledgeMetadataRecord.builder()
                .sequenceNumber(ack.getSeqno())
                .stream(ack.getStream())
                .domain(Optional.ofNullable(ack.getDomain()))
                .duplicate(ack.isDuplicate())
                .counterValue(Optional.ofNullable(ack.getVal()))
                .batchId(Optional.ofNullable(ack.getBatchId()))
                .batchSize(ack.getBatchSize())
                .build();
    }

    /**
     * Get the stream sequence number for the corresponding published message.
     *
     * @return the sequence number for the stored message.
     */
    long sequenceNumber();

    /**
     * Get the name of the stream a published message was stored in.
     *
     * @return the name of the stream.
     */
    @NonNull
    String stream();

    /**
     * Gets the domain of a stream
     *
     * @return the domain name
     */
    @NonNull
    Optional<String> domain();

    /**
     * Gets if the server detected the published message was a duplicate.
     *
     * @return true if the message is a duplicate, false otherwise.
     */
    boolean duplicate();

    /**
     * Gets a counter value. Only available on counter enabled streams
     *
     * @return the counter value as a string
     */
    @NonNull
    Optional<String> counterValue();

    /**
     * Gets the batch id. Only populated for batch publishes
     *
     * @return the batch id
     */
    @NonNull
    Optional<String> batchId();

    /**
     * Gets the batch size. Only populated for batch publishes.
     *
     * @return the size of the batch
     */
    int batchSize();

}
