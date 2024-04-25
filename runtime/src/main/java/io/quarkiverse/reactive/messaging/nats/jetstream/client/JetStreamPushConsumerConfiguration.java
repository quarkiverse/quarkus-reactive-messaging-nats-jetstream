package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.Optional;

public interface JetStreamPushConsumerConfiguration extends JetStreamConsumerConfiguration {

    Optional<Boolean> ordered();

    Optional<String> deliverGroup();

    Optional<Duration> flowControl();

    Optional<Duration> idleHeartbeat();

    Optional<Long> rateLimit();

    Optional<Boolean> headersOnly();
}
