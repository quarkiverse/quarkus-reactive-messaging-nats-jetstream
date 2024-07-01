package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import io.nats.client.api.DeliverPolicy;

public interface ConsumerConfiguration<T> {

    String name();

    String stream();

    String subject();

    Optional<Duration> ackTimeout();

    Optional<Class<T>> getPayloadType();

    Optional<DeliverPolicy> deliverPolicy();

    Optional<Long> startSequence();

    Optional<ZonedDateTime> startTime();

    Optional<Integer> maxAckPending();

    boolean traceEnabled();
}
