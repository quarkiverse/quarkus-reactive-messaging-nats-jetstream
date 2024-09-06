package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.Optional;

public interface PushConsumerConfiguration<T> {

    Optional<Boolean> ordered();

    Optional<Duration> flowControl();

    Optional<Duration> idleHeartbeat();

    Optional<Long> rateLimit();

    Optional<Boolean> headersOnly();

    Optional<String> deliverGroup();

    Optional<String> deliverSubject();

    ConsumerConfiguration<T> consumerConfiguration();

    String subject();
}
