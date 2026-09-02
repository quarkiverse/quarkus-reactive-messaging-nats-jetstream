package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record AcknowledgeMetadataImpl(
        long sequenceNumber,
        @NonNull String stream,
        @NonNull Optional<String> domain,
        boolean duplicate,
        @NonNull Optional<String> counterValue,
        @NonNull Optional<String> batchId,
        @NonNull Optional<Integer> batchSize) implements AcknowledgeMetadata {
}
