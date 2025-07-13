package io.quarkiverse.reactive.messaging.nats.jetstream.test.misc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.SubscribeException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusUnitTest;

public class PullSubscribeConnectionTest {
    private final static Logger logger = Logger.getLogger(PullSubscribeConnectionTest.class);

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestSpanExporter.class, Data.class))
            .withConfigurationResource("application-reader.properties");

    @Inject
    JetStreamConfiguration jetStreamConfiguration;

    @Inject
    ConnectionFactory connectionFactory;

    @Test
    void createConnectionWithModifiedConfigurationIsFailingWithSystemException() throws Exception {
        final var consumerConfiguration = createConsumerConfiguration(List.of(Duration.ofSeconds(10)), 2L);

        try (final var connection = connectionFactory.create(jetStreamConfiguration.connection()).await()
                .atMost(Duration.ofSeconds(30))) {
            logger.info("Connected to NATS");
            connection.subscribe("reader-test", "reader-data-consumer", consumerConfiguration).await()
                    .atMost(Duration.ofSeconds(30));
            final var consumer = connection.streamManagement()
                    .onItem()
                    .transformToUni(streamManagement -> streamManagement.getConsumer("reader-test",
                            "reader-data-consumer"))
                    .await().atMost(Duration.ofSeconds(30));
            assertThat(consumer).isNotNull();
            assertThat(consumer.configuration().backoff()).isEqualTo(List.of(Duration.ofSeconds(10)));
            assertThat(consumer.configuration().maxDeliver()).isEqualTo(2L);
        }

        final var updatedConsumerConfiguration = createConsumerConfiguration(
                List.of(Duration.ofSeconds(10), Duration.ofSeconds(30)), 3L);
        try (final var connection = connectionFactory.create(jetStreamConfiguration.connection(),
                new DefaultConnectionListener()).await().atMost(Duration.ofSeconds(30))) {
            assertThrows(SubscribeException.class,
                    () -> connection.subscribe("reader-test", "reader-data-consumer", updatedConsumerConfiguration).await()
                            .atMost(Duration.ofSeconds(30)));
        }
    }

    private PullConsumerConfiguration createConsumerConfiguration(List<Duration> backoff, Long maxDeliver) {
        return new PullConsumerConfiguration() {

            @Override
            public ConsumerConfiguration consumerConfiguration() {
                return new ConsumerConfiguration() {
                    @Override
                    public Boolean durable() {
                        return true;
                    }

                    @Override
                    public List<String> filterSubjects() {
                        return List.of("reader-data");
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
                        return Optional.of(maxDeliver);
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
                        return Optional.of(backoff);
                    }

                    @Override
                    public Optional<ZonedDateTime> pauseUntil() {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<Class<?>> payloadType() {
                        return Optional.of(Data.class);
                    }

                    @Override
                    public Optional<Duration> acknowledgeTimeout() {
                        return Optional.of(Duration.ofMillis(1000));
                    }
                };
            }

            @Override
            public PullConfiguration pullConfiguration() {
                return new PullConfiguration() {
                    @Override
                    public Duration maxExpires() {
                        return Duration.ofSeconds(3);
                    }

                    @Override
                    public Integer batchSize() {
                        return 100;
                    }

                    @Override
                    public Integer rePullAt() {
                        return 50;
                    }

                    @Override
                    public Optional<Integer> maxWaiting() {
                        return Optional.empty();
                    }
                };
            }

        };
    }
}
