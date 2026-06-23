package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.time.ZonedDateTime;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Metadata;
import lombok.Builder;

@Builder
public record ConsumerMetadata(@NonNull Integer deliveredCount,
        @NonNull String consumer,
        @NonNull Long streamSequence,
        @NonNull Long consumerSequence,
        @NonNull ZonedDateTime timestamp) implements Metadata {

    public ConsumerMetadata {
        Objects.requireNonNull(deliveredCount, "deliveredCount");
        Objects.requireNonNull(consumer, "consumer");
        Objects.requireNonNull(streamSequence, "streamSequence");
        Objects.requireNonNull(consumerSequence, "consumerSequence");
        Objects.requireNonNull(timestamp, "timestamp");
    }

}
