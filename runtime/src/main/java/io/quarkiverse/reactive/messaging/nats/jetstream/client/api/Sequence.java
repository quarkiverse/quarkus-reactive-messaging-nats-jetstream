package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.time.ZonedDateTime;

public record Sequence(long consumerSequence,
        long streamSequence,
        ZonedDateTime lastActive) {
}
