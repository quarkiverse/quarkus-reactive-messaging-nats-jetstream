package io.quarkiverse.reactive.messaging.nats.jetstream.test.fetch;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;

public record ConsumerConfiguration(String stream, String name, String subject)
        implements
            io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration {

    @Override
    public String name() {
        return name;
    }

    @Override
    public String stream() {
        return stream;
    }

    @Override
    public Boolean durable() {
        return true;
    }

    @Override
    public List<String> filterSubjects() {
        return List.of(subject);
    }

    @Override
    public Optional<Duration> ackWait() {
        return Optional.empty();
    }

    @Override
    public DeliverPolicy deliverPolicy() {
        return DeliverPolicy.All;
    }

    @Override
    public Optional<Long> startSequence() {
        return Optional.empty();
    }

    @Override
    public Optional<ZonedDateTime> startTime() {
        return Optional.empty();
    }

    @Override
    public Optional<String> description() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> inactiveThreshold() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> maxAckPending() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> maxDeliver() {
        return Optional.empty();
    }

    @Override
    public ReplayPolicy replayPolicy() {
        return ReplayPolicy.Instant;
    }

    @Override
    public Optional<Integer> replicas() {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> memoryStorage() {
        return Optional.empty();
    }

    @Override
    public Optional<String> sampleFrequency() {
        return Optional.empty();
    }

    @Override
    public Map<String, String> metadata() {
        return Map.of();
    }

    @Override
    public Optional<List<Duration>> backoff() {
        return Optional.empty();
    }

    @Override
    public Optional<ZonedDateTime> pauseUntil() {
        return Optional.empty();
    }

    @Override
    public Duration acknowledgeTimeout() {
        return Duration.ofMillis(1000);
    }
}
