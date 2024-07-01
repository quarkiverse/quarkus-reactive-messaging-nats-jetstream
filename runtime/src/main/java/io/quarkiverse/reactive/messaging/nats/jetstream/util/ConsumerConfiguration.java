package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.time.Duration;
import java.util.Optional;

public interface ConsumerConfiguration<T> {

    String name();

    String stream();

    String subject();

    Optional<Duration> ackTimeout();

    Optional<Class<T>> getPayloadType();

    boolean traceEnabled();
}
