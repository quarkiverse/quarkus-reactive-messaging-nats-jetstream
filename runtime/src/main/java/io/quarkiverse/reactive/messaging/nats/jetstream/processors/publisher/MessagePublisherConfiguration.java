package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamConsumerConfiguration;

public interface MessagePublisherConfiguration<T> extends JetStreamConsumerConfiguration {

    String channel();

    Optional<Class<T>> payloadType();

    Duration retryBackoff();

    boolean exponentialBackoff();

    Duration exponentialBackoffMaxDuration();

    boolean traceEnabled();

}
