package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushSubscribeOptionsFactory;

public class MessagePublisherProcessorTest {

    @Test
    void configureSubscriberClient() {
        final var durable = "durable";
        final var deleiverGroup = "deliver-group";
        final var backoff = List.of(Duration.parse("PT1S"));
        final var maxDeliever = 3L;

        final var factory = new PushSubscribeOptionsFactory();
        final var options = factory.create(new MessagePushPublisherConfiguration<>() {

            @Override
            public Optional<Boolean> ordered() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> flowControl() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> idleHeartbeat() {
                return Optional.empty();
            }

            @Override
            public Optional<Long> rateLimit() {
                return Optional.empty();
            }

            @Override
            public Optional<Boolean> headersOnly() {
                return Optional.empty();
            }

            @Override
            public String channel() {
                return "test";
            }

            @Override
            public Optional<String> deliverSubject() {
                return Optional.empty();
            }

            @Override
            public String subject() {
                return "test";
            }

            @Override
            public Duration retryBackoff() {
                return null;
            }

            @Override
            public Optional<String> deliverGroup() {
                return Optional.of(deleiverGroup);
            }

            @Override
            public ConsumerConfiguration<Object> consumerConfiguration() {
                return new ConsumerConfiguration<>() {

                    @Override
                    public String name() {
                        return durable;
                    }

                    @Override
                    public String stream() {
                        return "test";
                    }

                    @Override
                    public String subject() {
                        return "test";
                    }

                    @Override
                    public Optional<String> durable() {
                        return Optional.of(durable);
                    }

                    @Override
                    public Optional<Long> startSequence() {
                        return Optional.empty();
                    }

                    @Override
                    public List<Duration> backoff() {
                        return backoff;
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
                    public Optional<Duration> ackWait() {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<DeliverPolicy> deliverPolicy() {
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
                        return Optional.of(maxDeliever);
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
                    public Optional<Class<Object>> payloadType() {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<Duration> acknowledgeTimeout() {
                        return Optional.of(Duration.ofMillis(1000));
                    }
                };
            }
        });

        assertThat(options.getDurable()).isEqualTo(durable);
        assertThat(options.getDeliverGroup()).isEqualTo(deleiverGroup);
        assertThat(options.getConsumerConfiguration().getMaxDeliver()).isEqualTo(maxDeliever);
        assertThat(options.getConsumerConfiguration().getBackoff()).hasSize(1);
        assertThat(options.getConsumerConfiguration().getBackoff()).contains(Duration.ofSeconds(1));
    }
}
