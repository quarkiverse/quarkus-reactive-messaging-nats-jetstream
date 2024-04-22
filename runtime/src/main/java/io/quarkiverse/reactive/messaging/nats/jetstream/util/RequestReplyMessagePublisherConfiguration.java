package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamConsumerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePublisherConfiguration;

public class RequestReplyMessagePublisherConfiguration<T> implements MessagePublisherConfiguration<T> {
    private final RequestReplyConfiguration<T> requestReplyConfiguration;

    public RequestReplyMessagePublisherConfiguration(RequestReplyConfiguration<T> requestReplyConfiguration) {
        this.requestReplyConfiguration = requestReplyConfiguration;
    }

    @Override
    public String channel() {
        return requestReplyConfiguration.subject();
    }

    @Override
    public Optional<Class<T>> payloadType() {
        return Optional.of(requestReplyConfiguration.payloadType());
    }

    @Override
    public Duration retryBackoff() {
        return null;
    }

    @Override
    public boolean exponentialBackoff() {
        return false;
    }

    @Override
    public Duration exponentialBackoffMaxDuration() {
        return null;
    }

    @Override
    public boolean traceEnabled() {
        return requestReplyConfiguration.traceEnabled();
    }

    @Override
    public JetStreamConsumerType type() {
        return JetStreamConsumerType.Pull;
    }

    @Override
    public String stream() {
        return requestReplyConfiguration.stream();
    }

    @Override
    public String subject() {
        return requestReplyConfiguration.subject();
    }

    @Override
    public Optional<String> durable() {
        return Optional.empty();
    }

    @Override
    public List<String> filterSubjects() {
        return List.of();
    }

    @Override
    public Optional<Duration> ackWait() {
        return Optional.empty();
    }

    @Override
    public Optional<DeliverPolicy> deliverPolicy() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> startSeq() {
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
    public Optional<Integer> maxAckPending() {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> maxDeliver() {
        return requestReplyConfiguration.maxDeliver();
    }

    @Override
    public Optional<ReplayPolicy> replayPolicy() {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> replicas() {
        return Optional.of(requestReplyConfiguration.replicas());
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
    public List<Duration> backoff() {
        return List.of();
    }
}
