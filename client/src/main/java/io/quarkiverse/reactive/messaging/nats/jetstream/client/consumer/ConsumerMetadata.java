package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.time.ZonedDateTime;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Metadata;

public interface ConsumerMetadata extends Metadata {

    @NonNull
    Integer deliveredCount();

    @NonNull
    String consumer();

    @NonNull
    Long streamSequence();

    @NonNull
    Long consumerSequence();

    @NonNull
    ZonedDateTime timestamp();
}
