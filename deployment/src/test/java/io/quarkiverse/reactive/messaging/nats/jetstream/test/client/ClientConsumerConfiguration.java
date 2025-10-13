package io.quarkiverse.reactive.messaging.nats.jetstream.test.client;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ClientConsumerConfiguration<T>(String stream, String consumer, List<String> subjects) implements ConsumerConfiguration<T> {

    @Override
    public String name() {
        return consumer;
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
        return subjects;
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
    public Integer replicas() {
        return 1;
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
    public Optional<Class<T>> payloadType() {
        return Optional.empty();
    }

    @Override
    public Duration acknowledgeTimeout() {
        return Duration.ofSeconds(5);
    }
}
