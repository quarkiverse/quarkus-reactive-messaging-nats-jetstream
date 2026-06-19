package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.Objects;
import java.util.Optional;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import io.nats.client.api.PublishAck;

@Builder
public record AcknowledgeMetadata(
        /*
         * The stream sequence number for the corresponding published message.
         */
        long sequenceNumber,
        /*
         * The name of the stream a published message was stored in.
         */
        @NonNull String stream,
        /*
         * The domain of a stream
         */
        @NonNull
        Optional<String> domain,
        /*
         * The server detected the published message was a duplicate.
         */
        boolean duplicate,
        /*
         * The counter value. Only available on counter enabled streams
         */
        @NonNull
        Optional<String> counterValue,
        /*
         * The batch id. Only populated for batch publishes
         */
        @NonNull
        Optional<String> batchId,
        /*
         * Gets the batch size. Only populated for batch publishes.
         *
         * @return the size of the batch
         */
        int batchSize) implements Metadata {

    public AcknowledgeMetadata {
        Objects.requireNonNull(stream, "stream");
        Objects.requireNonNull(domain, "domain");
        Objects.requireNonNull(counterValue, "counterValue");
        Objects.requireNonNull(batchId, "batchId");
    }

    static AcknowledgeMetadata of(PublishAck ack) {
        return AcknowledgeMetadata.builder()
                .sequenceNumber(ack.getSeqno())
                .stream(ack.getStream())
                .domain(Optional.ofNullable(ack.getDomain()))
                .duplicate(ack.isDuplicate())
                .counterValue(Optional.ofNullable(ack.getVal()))
                .batchId(Optional.ofNullable(ack.getBatchId()))
                .batchSize(ack.getBatchSize())
                .build();
    }
}
