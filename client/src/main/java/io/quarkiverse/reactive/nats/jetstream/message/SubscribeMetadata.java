package io.quarkiverse.reactive.nats.jetstream.message;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

public interface SubscribeMetadata {

    String stream();

    String subject();

    String messageId();

    Integer deliveredCount();

    String consumer();

    Long streamSequence();

    Long consumerSequence();

    ZonedDateTime timestamp();

    @NonNull List<Duration> backoff();

}
