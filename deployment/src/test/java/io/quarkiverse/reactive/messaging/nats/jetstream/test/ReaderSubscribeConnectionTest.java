package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ReaderConsumerConfiguration;
import io.quarkus.test.QuarkusUnitTest;

public class ReaderSubscribeConnectionTest {
    private final static Logger logger = Logger.getLogger(ReaderSubscribeConnectionTest.class);

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .withConfigurationResource("application-reader.properties");

    @Inject
    NatsConfiguration natsConfiguration;

    @Inject
    ConnectionFactory connectionFactory;

    @Test
    void createConnectionWithModifiedConfiguration() throws Exception {
        var consumerConfiguration = createConsumerConfiguration(List.of(Duration.ofSeconds(10)), 2L);

        try (final var connection = connectionFactory.create(ConnectionConfiguration.of(natsConfiguration),
                new DefaultConnectionListener()).await().atMost(Duration.ofSeconds(30))) {
            logger.info("Connected to NATS");
            try (final var ignored = connection.subscription(consumerConfiguration).await().atMost(Duration.ofSeconds(30))) {
                final var consumer = connection.getConsumer("reader-test", consumerConfiguration.consumerConfiguration().name())
                        .await().atMost(Duration.ofSeconds(30));
                assertThat(consumer).isNotNull();
                assertThat(consumer.configuration().backoff()).isEqualTo(List.of(Duration.ofSeconds(10)));
                assertThat(consumer.configuration().maxDeliver()).isEqualTo(2L);
            }
        }

        consumerConfiguration = createConsumerConfiguration(List.of(Duration.ofSeconds(10), Duration.ofSeconds(30)), 3L);
        try (final var connection = connectionFactory.create(ConnectionConfiguration.of(natsConfiguration),
                new DefaultConnectionListener()).await().atMost(Duration.ofSeconds(30))) {
            try (final var ignored = connection.subscription(consumerConfiguration).await().atMost(Duration.ofSeconds(30))) {
                logger.info("Connected to NATS");
                final var consumer = connection.getConsumer("reader-test", consumerConfiguration.consumerConfiguration().name())
                        .await().atMost(Duration.ofSeconds(30));
                assertThat(consumer).isNotNull();
                assertThat(consumer.configuration().backoff())
                        .isEqualTo(List.of(Duration.ofSeconds(10), Duration.ofSeconds(30)));
                assertThat(consumer.configuration().maxDeliver()).isEqualTo(3L);
            }
        }
    }

    private ReaderConsumerConfiguration<Object> createConsumerConfiguration(List<Duration> backoff, Long maxDeliver) {
        return new ReaderConsumerConfiguration<>() {
            @Override
            public String subject() {
                return "reader-data";
            }

            @Override
            public Integer rePullAt() {
                return 50;
            }

            @Override
            public Integer maxRequestBatch() {
                return 100;
            }

            @Override
            public Optional<Integer> maxWaiting() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> maxRequestExpires() {
                return Optional.empty();
            }

            @Override
            public ConsumerConfiguration<Object> consumerConfiguration() {
                return new ConsumerConfiguration<>() {
                    @Override
                    public String name() {
                        return "reader-data-consumer";
                    }

                    @Override
                    public String stream() {
                        return "reader-test";
                    }

                    @Override
                    public Optional<String> durable() {
                        return Optional.of("reader-data-consumer");
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
                        return Optional.of(maxDeliver);
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
                    public Optional<Class<Object>> payloadType() {
                        return Optional.of(Object.class);
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
                    public Duration ackTimeout() {
                        return Duration.ofSeconds(5);
                    }
                };
            }
        };
    }
}
