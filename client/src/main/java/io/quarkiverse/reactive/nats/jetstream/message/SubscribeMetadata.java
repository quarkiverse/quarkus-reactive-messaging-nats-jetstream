package io.quarkiverse.reactive.nats.jetstream.message;

import org.jspecify.annotations.NonNull;

import java.time.ZonedDateTime;

public interface SubscribeMetadata extends Metadata {

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
