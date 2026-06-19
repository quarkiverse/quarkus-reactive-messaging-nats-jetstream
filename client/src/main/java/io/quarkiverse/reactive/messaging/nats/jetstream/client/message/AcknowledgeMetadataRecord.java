package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.Optional;

import io.nats.client.api.PublishAck;
import lombok.Builder;

@Builder
record AcknowledgeMetadataRecord(long sequenceNumber,
        String stream,
        Optional<String> domain,
        boolean duplicate,
        Optional<String> counterValue,
        Optional<String> batchId,
        int batchSize) implements AcknowledgeMetadata {

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
}
