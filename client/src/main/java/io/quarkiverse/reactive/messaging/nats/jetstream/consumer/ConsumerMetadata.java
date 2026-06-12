package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import io.quarkiverse.reactive.messaging.nats.jetstream.message.Metadata;
import org.jspecify.annotations.NonNull;

import java.time.ZonedDateTime;

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
