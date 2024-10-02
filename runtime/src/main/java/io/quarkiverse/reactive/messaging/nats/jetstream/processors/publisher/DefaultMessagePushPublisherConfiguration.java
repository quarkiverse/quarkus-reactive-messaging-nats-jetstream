package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;

public class DefaultMessagePushPublisherConfiguration<T> implements MessagePushPublisherConfiguration<T> {
    private final JetStreamConnectorIncomingConfiguration configuration;

    public DefaultMessagePushPublisherConfiguration(JetStreamConnectorIncomingConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Optional<Boolean> ordered() {
        return configuration.getPushOrdered();
    }

    @Override
    public Optional<Duration> flowControl() {
        return configuration.getPushFlowControl().map(Duration::parse);
    }

    @Override
    public Optional<String> deliverGroup() {
        return configuration.getPushDeliverGroup();
    }

    @Override
    public String subject() {
        return configuration.getSubject().orElseThrow(() -> new IllegalStateException("No subject configured"));
    }

    @Override
    public Optional<String> deliverSubject() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> idleHeartbeat() {
        return configuration.getPushIdleHeartBeat().map(Duration::parse);
    }

    @Override
    public Optional<Long> rateLimit() {
        return configuration.getPushRateLimit();
    }

    @Override
    public Optional<Boolean> headersOnly() {
        return Optional.empty();
    }

    @Override
    public String channel() {
        return configuration.getChannel();
    }

    @Override
    public Duration retryBackoff() {
        return Duration.ofMillis(configuration.getRetryBackoff());
    }

    @Override
    public ConsumerConfiguration<T> consumerConfiguration() {
        return new ConsumerConfiguration<>() {
            @Override
            public Optional<String> name() {
                return configuration.getName();
            }

            @Override
            public String stream() {
                return configuration.getStream().orElseThrow(() -> new IllegalStateException("No stream configured"));
            }

            @Override
            public Optional<String> durable() {
                return configuration.getDurable();
            }

            @Override
            public List<String> filterSubjects() {
                return configuration.getFilterSubjects().map(filterSubjects -> List.of(filterSubjects.split(",")))
                        .orElseGet(List::of);
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
            public Optional<ZonedDateTime> startTime() {
                return Optional.empty();
            }

            @Override
            public Optional<String> description() {
                return configuration.getDescription();
            }

            @Override
            public Optional<Duration> inactiveThreshold() {
                return configuration.getInactiveThreshold().map(Duration::parse);
            }

            @Override
            public Optional<Long> maxAckPending() {
                return configuration.getMaxAckPending();
            }

            @Override
            public Optional<Long> maxDeliver() {
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
            public Optional<Long> startSequence() {
                return Optional.empty();
            }

            @Override
            public Optional<AckPolicy> ackPolicy() {
                return Optional.empty();
            }

            @Override
            public Optional<ZonedDateTime> pauseUntil() {
                return Optional.empty();
            }

            @Override
            public Optional<Class<T>> payloadType() {
                return configuration.getPayloadType().map(PayloadMapper::loadClass);
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
            public boolean traceEnabled() {
                return configuration.getTraceEnabled();
            }

            @Override
            public Duration ackTimeout() {
                return Duration.parse(configuration.getAckTimeout());
            }

            private List<Duration> getBackOff(List<String> backoff) {
                if (backoff == null || backoff.isEmpty()) {
                    return List.of();
                } else {
                    return backoff.stream().map(Duration::parse).toList();
                }
            }
        };
    }
}
