package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api;

import java.time.ZonedDateTime;
import java.util.Optional;

import lombok.Builder;

@Builder
record SequenceImpl(long consumerSequence,
        long streamSequence,
        Optional<ZonedDateTime> lastActive) implements Sequence {
}
