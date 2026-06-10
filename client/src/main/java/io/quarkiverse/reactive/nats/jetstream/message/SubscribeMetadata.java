package io.quarkiverse.reactive.nats.jetstream.message;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import org.jspecify.annotations.NonNull;

public interface SubscribeMetadata {

    @NonNull
    String stream();

    @NonNull
    String subject();

    @NonNull
    String messageId();

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
