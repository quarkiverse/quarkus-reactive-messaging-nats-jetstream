package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

record PullOptionsImpl(@NonNull Optional<Long> maxWaiting,
        @NonNull Optional<Duration> maxExpires,
        @NonNull Optional<Long> maxBatch,
        @NonNull Optional<Long> maxBytes) implements PullOptions {
}
