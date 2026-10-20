package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.api;

import java.time.ZonedDateTime;
import java.util.Optional;

import lombok.Builder;

@Builder
record MessageImpl(Optional<String> subject,
        long sequence,
        Optional<byte[]> payload,
        Optional<ZonedDateTime> timestamp,
        Headers headers,
        Optional<String> stream,
        long lastSequence,
        long numberOfPendingMessages,
        Optional<Status> status) implements Message {
}
