package io.quarkiverse.reactive.messaging.nats.jetstream.test.client;

import java.time.Duration;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PushConfiguration;

public record ClientPushConfiguration(String deliverSubject) implements PushConfiguration {

    @Override
    public String deliverSubject() {
        return deliverSubject;
    }

    @Override
    public Optional<Duration> flowControl() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> idleHeartbeat() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> rateLimit() {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> headersOnly() {
        return Optional.empty();
    }

    @Override
    public Optional<String> deliverGroup() {
        return Optional.empty();
    }
}
