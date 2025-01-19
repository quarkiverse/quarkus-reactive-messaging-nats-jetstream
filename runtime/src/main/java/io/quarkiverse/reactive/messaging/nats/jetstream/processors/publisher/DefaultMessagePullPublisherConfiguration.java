package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.DefaultPayloadMapper;
import io.quarkus.runtime.configuration.DurationConverter;

public class DefaultMessagePullPublisherConfiguration<T> implements MessagePullPublisherConfiguration<T> {
    private final JetStreamConnectorIncomingConfiguration configuration;

    public DefaultMessagePullPublisherConfiguration(JetStreamConnectorIncomingConfiguration configuration) {
        this.configuration = configuration;
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
    public String subject() {
        return configuration.getSubject().orElseThrow(() -> new IllegalStateException("No subject configured"));
    }

    @Override
    public Duration maxExpires() {
        final var converter = new DurationConverter();
        return configuration.getPullMaxExpires().map(converter::convert).orElse(null);
    }

    @Override
    public Integer batchSize() {
        return configuration.getPullBatchSize();
    }

    @Override
    public Optional<Integer> maxWaiting() {
        return configuration.getPullMaxWaiting();
    }

    @Override
    public ConsumerConfiguration<T> consumerConfiguration() {
        return new ConsumerConfiguration<>() {
            @Override
            public String name() {
                return configuration.getName()
                        .orElseGet(() -> durable().orElseGet(() -> String.format("%s-consumer", subject())
                                .replace("*", "")
                                .replace(".", "")
                                .replace(">", "")
                                .replace("\\", "")
                                .replace("/", "")));
            }

            @Override
            public String stream() {
                return configuration.getStream().orElseThrow(() -> new IllegalStateException("No stream configured"));
            }

            @Override
            public String subject() {
                return configuration.getSubject().orElseThrow(() -> new IllegalStateException("No subject configured"));
            }

            @Override
            public Optional<String> durable() {
                return configuration.getDurable();
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
                        .map(this::of)
                        .orElseGet(List::of);
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
                return configuration.getPayloadType().map(DefaultPayloadMapper::loadClass);
            }

            private List<Duration> of(List<String> values) {
                final var converter = new DurationConverter();
                if (values == null || values.isEmpty()) {
                    return List.of();
                } else {
                    return values.stream().map(converter::convert).toList();
                }
            }
        };
    }
}
