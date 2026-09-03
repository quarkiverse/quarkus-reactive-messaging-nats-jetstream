package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.fetch;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.DeliverPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.PullOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.PushOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ReplayPolicy;

public record ConsumerConfiguration(String name,
        String subject)
        implements
            io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration {

    @Override
    public @NonNull String name() {
        return name;
    }

    @Override
    public boolean durable() {
        return true;
    }

    @NonNull
    @Override
    public Optional<Set<String>> filterSubjects() {
        return Optional.of(Set.of(subject));
    }

    @NonNull
    @Override
    public DeliverPolicy deliverPolicy() {
        return DeliverPolicy.All;
    }

    @Override
    public long startSequence() {
        return 0;
    }

    @NonNull
    @Override
    public Optional<ZonedDateTime> startTime() {
        return Optional.empty();
    }

    @NonNull
    @Override
    public Optional<String> description() {
        return Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Duration> inactiveThreshold() {
        return Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Long> maxDeliver() {
        return Optional.empty();
    }

    @NonNull
    @Override
    public ReplayPolicy replayPolicy() {
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

    @NonNull
    @Override
    public Optional<String> sampleFrequency() {
        return Optional.empty();
    }

    @NonNull
    @Override
    public Map<String, String> metadata() {
        return Map.of();
    }

    @NonNull
    @Override
    public Optional<List<Duration>> backoff() {
        return Optional.empty();
    }

    @NonNull
    @Override
    public Optional<ZonedDateTime> pauseUntil() {
        return Optional.empty();
    }

    @NonNull
    @Override
    public Duration acknowledgeTimeout() {
        return Duration.ofMillis(1000);
    }

    @Override
    public @NonNull Optional<String> filterSubject() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Duration> acknowledgeWait() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Long> maxAcknowledgePending() {
        return Optional.empty();
    }

    @Override
    public boolean headersOnly() {
        return false;
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
