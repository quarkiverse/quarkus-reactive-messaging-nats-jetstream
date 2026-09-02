package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

record PushOptionsImpl(@NonNull String deliverSubject,
        boolean flowControl,
        @NonNull Optional<Duration> idleHeartbeat,
        @NonNull Optional<Long> rateLimit,
        @NonNull Optional<String> deliverGroup) implements PushOptions {
}
