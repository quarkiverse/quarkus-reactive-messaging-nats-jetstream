package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamConsumerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;

public abstract class AbstractMessagePublisherConfiguration implements MessagePublisherConfiguration {
    protected final JetStreamConnectorIncomingConfiguration configuration;

    public AbstractMessagePublisherConfiguration(final JetStreamConnectorIncomingConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String channel() {
        return configuration.getChannel();
    }

    @Override
    public Optional<Class> payloadType() {
        return configuration.getPayloadType().map(PayloadMapper::loadClass);
    }

    @Override
    public Duration retryBackoff() {
        return Duration.ofMillis(configuration.getRetryBackoff());
    }

    @Override
    public boolean exponentialBackoff() {
        return configuration.getExponentialBackoff() != null ? configuration.getExponentialBackoff() : false;
    }

    @Override
    public Duration exponentialBackoffMaxDuration() {
        return configuration.getExponentialBackoffMaxDuration() != null
                ? Duration.parse(configuration.getExponentialBackoffMaxDuration())
                : null;
    }

    @Override
    public JetStreamConsumerType type() {
        return JetStreamConsumerType.valueOf(configuration.getPublisherType());
    }

    @Override
    public String stream() {
        return configuration.getStream().orElseThrow(() -> new IllegalArgumentException("No stream configured"));
    }

    @Override
    public String subject() {
        return configuration.getSubject().orElseThrow(() -> new IllegalArgumentException("No subject configured"));
    }

    @Override
    public Optional<String> durable() {
        return configuration.getDurable();
    }

    @Override
    public List<String> filterSubjects() {
        return configuration.getFilterSubjects().map(filterSubjects -> List.of(filterSubjects.split(","))).orElseGet(List::of);
    }

    @Override
    public Optional<Duration> ackWait() {
        return configuration.getAckWait().map(Duration::parse);
    }

    @Override
    public Optional<DeliverPolicy> deliverPolicy() {
        return configuration.getDeliverPolicy().map(DeliverPolicy::valueOf);
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
        return configuration.getDescription();
    }

    @Override
    public Optional<Duration> inactiveThreshold() {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> maxAckPending() {
        return configuration.getMaxAckPending();
    }

    @Override
    public Integer maxDeliver() {
        return configuration.getMaxDeliver();
    }

    @Override
    public Optional<ReplayPolicy> replayPolicy() {
        return configuration.getReplayPolicy().map(ReplayPolicy::valueOf);
    }

    @Override
    public Optional<Integer> replicas() {
        return configuration.getReplicas();
    }

    @Override
    public Optional<Boolean> memoryStorage() {
        return configuration.getMemoryStorage();
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
        return configuration.getBackOff()
                .map(backoff -> backoff.split(","))
                .map(List::of)
                .map(this::getBackOff)
                .orElseGet(Collections::emptyList);
    }

    @Override
    public boolean traceEnabled() {
        return configuration.getTraceEnabled() != null ? configuration.getTraceEnabled() : true;
    }

    protected List<Duration> getBackOff(List<String> backoff) {
        if (backoff == null || backoff.isEmpty()) {
            return List.of();
        } else {
            return backoff.stream().map(this::toDuration).toList();
        }
    }

    protected Duration toDuration(String value) {
        return Duration.parse(value);
    }
}
