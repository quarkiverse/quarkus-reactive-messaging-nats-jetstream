package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.time.Duration;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.JetStreamConsumerConfiguration;

public interface ConsumerConfiguration<T> extends JetStreamConsumerConfiguration {

    Optional<Duration> ackTimeout();

    Optional<Class<T>> getPayloadType();

    boolean traceEnabled();

}
