package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.time.ZonedDateTime;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
record MessageMetadataImpl(int deliveredCount,
        @NonNull String stream,
        @NonNull String consumer,
        long streamSequence,
        long consumerSequence,
        @NonNull ZonedDateTime timestamp) implements MessageMetadata {
}
