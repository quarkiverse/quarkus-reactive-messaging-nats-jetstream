package io.quarkiverse.reactive.messaging.nats.jetstream.client.administration;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record Sequence(long consumerSequence,
                       long streamSequence,
                       ZonedDateTime lastActive) {
}
