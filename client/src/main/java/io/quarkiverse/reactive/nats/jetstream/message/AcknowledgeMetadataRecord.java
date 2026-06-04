package io.quarkiverse.reactive.nats.jetstream.message;

import lombok.Builder;

import java.util.Optional;

@Builder
record AcknowledgeMetadataRecord(long sequenceNumber,
                                        String stream,
                                        Optional<String> domain,
                                        boolean duplicate,
                                        Optional<String> counterValue,
                                        Optional<String> batchId,
                                        int batchSize) implements AcknowledgeMetadata {
}
