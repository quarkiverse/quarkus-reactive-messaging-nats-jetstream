package io.quarkiverse.reactive.messaging.nats.jetstream.setup;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamConsumerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamPullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.RequestReplyConfiguration;

public class RequestReplyPullConsumerConfiguration<T> implements JetStreamPullConsumerConfiguration {
    private final RequestReplyConfiguration<T> requestReplyConfiguration;

    public RequestReplyPullConsumerConfiguration(RequestReplyConfiguration<T> requestReplyConfiguration) {
        this.requestReplyConfiguration = requestReplyConfiguration;
    }

    @Override
    public Optional<Integer> maxWaiting() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> maxRequestExpires() {
        return requestReplyConfiguration.maxRequestExpires();
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
        return requestReplyConfiguration.durable();
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
    public List<Duration> backoff() {
        return List.of();
    }
}
