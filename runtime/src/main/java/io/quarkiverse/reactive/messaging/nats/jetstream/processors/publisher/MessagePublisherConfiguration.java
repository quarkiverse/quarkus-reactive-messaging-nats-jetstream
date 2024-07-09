package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.Optional;

public interface MessagePublisherConfiguration<T> {

    String channel();

    String subject();

    Optional<Class<T>> payloadType();

    Duration retryBackoff();

    boolean exponentialBackoff();

    Duration exponentialBackoffMaxDuration();

    boolean traceEnabled();

    Duration ackTimeout();
}
