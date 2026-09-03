package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.nats.client.api.PublishAck;

public interface AcknowledgeMetadata extends Metadata {

    /*
     * The stream sequence number for the corresponding published message.
     *
     * @return the stream sequence number for the corresponding published message
     */
    long sequenceNumber();

    /*
     * The name of the stream a published message was stored in.
     *
     * @return the name of the stream a published message was stored in
     */
    @NonNull
    String stream();

    /*
     * The domain of a stream
     *
     * @return the domain of a stream
     */
    @NonNull
    Optional<String> domain();

    /*
     * The server detected the published message was a duplicate.
     *
     * @return true if the server detected the published message was a duplicate
     */
    boolean duplicate();

    /*
     * The counter value. Only available on counter enabled streams
     *
     * @return the counter value. Only available on counter enabled streams
     */
    @NonNull
    Optional<String> counterValue();

    /*
     * The batch id. Only populated for batch publishes
     *
     * @return the batch id. Only populated for batch publishes
     */
    @NonNull
    Optional<String> batchId();

    /*
     * Gets the batch size. Only populated for batch publishes.
     *
     * @return the batch size. Only populated for batch publishes
     */
    @NonNull
    Optional<Integer> batchSize();

    static AcknowledgeMetadata of(PublishAck ack) {
        return AcknowledgeMetadataImpl.builder()
                .sequenceNumber(ack.getSeqno())
                .stream(ack.getStream())
                .domain(Optional.ofNullable(ack.getDomain()))
                .duplicate(ack.isDuplicate())
                .counterValue(Optional.ofNullable(ack.getVal()))
                .batchId(Optional.ofNullable(ack.getBatchId()))
                .batchSize(Optional.of(ack.getBatchSize()).filter(value -> value > 0))
                .build();
    }
}
