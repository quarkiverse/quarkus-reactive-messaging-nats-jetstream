package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import java.util.Optional;

import lombok.Builder;

@Builder
record AcknowledgeMetadataRecord(long sequenceNumber,
        String stream,
        Optional<String> domain,
        boolean duplicate,
        Optional<String> counterValue,
        Optional<String> batchId,
        int batchSize) implements AcknowledgeMetadata {
}
