package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.time.Duration;
import java.util.Optional;

import lombok.Builder;

@Builder
public record PushConfigurationImpl(Boolean ordered,
        String deliverSubject,
        Optional<Duration> flowControl,
        Optional<Duration> idleHeartbeat,
        Optional<Long> rateLimit,
        Optional<Boolean> headersOnly,
        Optional<String> deliverGroup) implements PushConfiguration {
}
