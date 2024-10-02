package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.time.ZonedDateTime;

import lombok.Builder;

@Builder
public record Sequence(long consumerSequence,
        long streamSequence,
        ZonedDateTime lastActive) {
}
