package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.*;

public record ClientConsumerConfiguration(String consumer, Set<String> subjects) implements ConsumerConfiguration {

    @Override
    public @NonNull String name() {
        return consumer;
    }

    @Override
    public boolean durable() {
        return true;
    }

    @Override
    public @NonNull Optional<String> filterSubject() {
        return subjects.size() == 1 ? Optional.of(subjects.iterator().next()) : Optional.empty();
    }

    @Override
    public @NonNull Optional<Set<String>> filterSubjects() {
        return subjects.size() == 1 ? Optional.empty() : Optional.of(subjects);
    }

    @Override
    public @NonNull Optional<Duration> acknowledgeWait() {
        return Optional.empty();
    }

    @Override
    public @NonNull Duration acknowledgeTimeout() {
        return Duration.ofSeconds(10);
    }

    @Override
    public @NonNull DeliverPolicy deliverPolicy() {
        return DeliverPolicy.All;
    }

    @Override
    public long startSequence() {
        return 0;
    }

    @Override
    public @NonNull Optional<ZonedDateTime> startTime() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<String> description() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Duration> inactiveThreshold() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Long> maxAcknowledgePending() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Long> maxDeliver() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<List<Duration>> backoff() {
        return Optional.empty();
    }

    @Override
    public @NonNull ReplayPolicy replayPolicy() {
        return ReplayPolicy.Instant;
    }

    @Override
    public @NonNull Optional<Integer> replicas() {
        return Optional.empty();
    }

    @Override
    public boolean memoryStorage() {
        return false;
    }

    @Override
    public @NonNull Optional<String> sampleFrequency() {
        return Optional.empty();
    }

    @Override
    public @NonNull Map<String, String> metadata() {
        return Map.of();
    }

    @Override
    public boolean headersOnly() {
        return false;
    }

    @Override
    public @NonNull Optional<ZonedDateTime> pauseUntil() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<PullOptions> pullOptions() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<PushOptions> pushOptions() {
        return Optional.empty();
    }
}
