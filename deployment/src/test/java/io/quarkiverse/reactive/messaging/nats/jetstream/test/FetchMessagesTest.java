package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.StreamManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.FetchConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.TestSpanExporter;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Uni;

public class FetchMessagesTest {

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addClasses(TestSpanExporter.class, Data.class))
            .withConfigurationResource("application-fetch.properties");

    @Inject
    NatsConfiguration natsConfiguration;

    @Inject
    ConnectionFactory connectionFactory;

    @BeforeEach
    public void setup() throws Exception {
        try (final var connection = connectionFactory.create(ConnectionConfiguration.of(natsConfiguration),
                new DefaultConnectionListener()).await().atMost(Duration.ofSeconds(30))) {
            connection.streamManagement().onItem().transformToUni(StreamManagement::purgeAllStreams)
                    .await().atMost(Duration.ofSeconds(30));
        }
    }

    @Test
    void fetchOneMessage() throws Exception {
        final var data = new Data("test", "52b13992-749a-4943-ab8f-2403c734c648", "46c818c9-8915-48a6-9378-b8f540b0afe2");
        publish(data, "fetch-data");

        final var received = next("fetch-data", true);
        assertThat(received).isEqualTo(data);
    }

    @Test
    void fetchTwoMessages() throws Exception {
        final var data1 = new Data("test1", "ea030796-4692-40f1-9ce5-a9cf04b5fe53", "3bd00e71-7bc3-45c3-a1aa-8f8679ff7373");
        final var data2 = new Data("test2", "4d76e337-97f8-41b9-9030-b19d4ba824be", "58707f28-74c5-45fd-b59a-be0286bb8490");

        publish(data1, "fetch-data");
        publish(data2, "fetch-data");

        final var received1 = next("fetch-data", true);
        assertThat(received1).isEqualTo(data1);

        final var received2 = next("fetch-data", true);
        assertThat(received2).isEqualTo(data2);
    }

    @Test
    void fetchOneNotAcknowledgedMessage() throws Exception {
        final var data1 = new Data("test1", "ea030796-4692-40f1-9ce5-a9cf04b5fe53", "3bd00e71-7bc3-45c3-a1aa-8f8679ff7373");
        final var data2 = new Data("test2", "4d76e337-97f8-41b9-9030-b19d4ba824be", "58707f28-74c5-45fd-b59a-be0286bb8490");

        publish(data1, "fetch-data");
        publish(data2, "fetch-data");

        final var received1 = next("fetch-data", false);
        assertThat(received1).isEqualTo(data1);

        final var received2 = next("fetch-data", true);
        assertThat(received2).isEqualTo(data1);
    }

    @Test
    void subjectTokens() throws Exception {
        final var data1 = new Data("test1", "14e9aaaf-0d42-42a8-a93a-ebe37ff6a742", "974932c1-90b8-4b79-b10a-b508d7badc04");
        final var data2 = new Data("test2", "8a6faacc-f05e-44f1-bc4b-70abbd5a1b50", "057e17c7-55ad-4970-841a-ad712048e0e1");
        final var data3 = new Data("test3", "14e9aaaf-0d42-42a8-a93a-ebe37ff6a742", "974932c1-90b8-4b79-b10a-b508d7badc04");
        final var data4 = new Data("test4", "5246354a-2342-4422-9268-af95862b51fb", "1325e196-4186-47ab-8b30-5047cae77d7e");

        publish(data1, "resources." + data1.resourceId());
        publish(data2, "resources." + data2.resourceId());
        publish(data3, "resources." + data3.resourceId());
        publish(data4, "resources." + data4.resourceId());

        final var received1 = next("resources." + data1.resourceId(), true);
        assertThat(received1).isEqualTo(data1);

        final var received2 = next("resources." + data2.resourceId(), true);
        assertThat(received2).isEqualTo(data2);

        final var received3 = next("resources." + data3.resourceId(), true);
        assertThat(received3).isEqualTo(data3);

        final var received4 = next("resources." + data4.resourceId(), true);
        assertThat(received4).isEqualTo(data4);
    }

    @Test
    void addAndRemoveSubject() throws Exception {
        final var data1 = new Data("test1", "64a8903f-983a-4775-8c41-e59c1a40ca08", "5a6af883-2be2-4c73-9d5d-7cdc4157f2fb");
        final var data2 = new Data("test2", "7a229cc2-e8e4-4a59-ba0a-40e878c9b3af", "d38ddb6f-3b9c-4a6c-978e-e97c0b66a2fd");

        addSubject(data1.resourceId());
        addSubject(data2.resourceId());

        publish(data1, data1.resourceId());
        publish(data2, data2.resourceId());

        final var received1 = next(data1.resourceId(), true);
        assertThat(received1).isEqualTo(data1);

        final var received2 = next(data2.resourceId(), true);
        assertThat(received2).isEqualTo(data2);

        removeSubject(data1.resourceId());
        removeSubject(data2.resourceId());
    }

    @Test
    void fetchMessages() throws Exception {
        final var data1 = new Data("test1", "64a8903f-983a-4775-8c41-e59c1a40ca08", "5a6af883-2be2-4c73-9d5d-7cdc4157f2fb");
        final var data2 = new Data("test2", "64a8903f-983a-4775-8c41-e59c1a40ca08", "d38ddb6f-3b9c-4a6c-978e-e97c0b66a2fd");

        addSubject(data1.resourceId());

        publish(data1, data1.resourceId());
        publish(data2, data2.resourceId());

        final var received = fetch(data1.resourceId(), true);
        assertThat(received).containsExactly(data1, data2);
    }

    private void addSubject(String subject) throws Exception {
        try (final var connection = connectionFactory.create(ConnectionConfiguration.of(natsConfiguration),
                new DefaultConnectionListener()).await().atMost(Duration.ofSeconds(30))) {
            connection.streamManagement()
                    .onItem().transformToUni(streamManagement -> streamManagement.addSubject("fetch-test", subject))
                    .await().atMost(Duration.ofSeconds(30));
        }
    }

    private void removeSubject(String subject) throws Exception {
        try (final var connection = connectionFactory.create(ConnectionConfiguration.of(natsConfiguration)).await()
                .atMost(Duration.ofSeconds(30))) {
            connection.streamManagement()
                    .onItem().transformToUni(streamManagement -> streamManagement.removeSubject("fetch-test", subject))
                    .await().atMost(Duration.ofSeconds(30));
        }
    }

    private void publish(Data data, String subject) throws Exception {
        try (final var connection = connectionFactory.<Data> create(ConnectionConfiguration.of(natsConfiguration),
                new DefaultConnectionListener()).await().atMost(Duration.ofSeconds(30))) {
            final var publishConfiguragtion = createPublishConfiguration(subject);
            final var consumerConfiguration = createConsumerConfiguration(subject);
            connection.publish(Message.of(data), publishConfiguragtion, consumerConfiguration)
                    .await()
                    .atMost(Duration.ofSeconds(30));
        }
    }

    private Data next(String subject, boolean ack) throws Exception {
        try (final var connection = connectionFactory.<Data> create(ConnectionConfiguration.of(natsConfiguration),
                new DefaultConnectionListener()).await().atMost(Duration.ofSeconds(30))) {
            final var consumerConfiguration = createConsumerConfiguration(subject);
            final var received = connection.next(consumerConfiguration, Duration.ofSeconds(30))
                    .await().atMost(Duration.ofSeconds(30));
            if (ack) {
                Uni.createFrom().completionStage(received.ack()).await().atMost(Duration.ofSeconds(30));
            } else {
                Uni.createFrom().completionStage(received.nack(new RuntimeException())).await().atMost(Duration.ofSeconds(30));
            }
            return received.getPayload();
        }
    }

    private List<Data> fetch(String subject, boolean ack) throws Exception {
        try (final var connection = connectionFactory.<Data> create(ConnectionConfiguration.of(natsConfiguration),
                new DefaultConnectionListener()).await().atMost(Duration.ofSeconds(30))) {
            final var consumerConfiguration = createFetchConsumerConfiguration(subject);
            final var received = connection.fetch(consumerConfiguration)
                    .onItem().transformToUniAndMerge(message -> {
                        if (ack) {
                            return Uni.createFrom().completionStage(message.ack())
                                    .onItem().transform(ignored -> message);
                        } else {
                            return Uni.createFrom().completionStage(message.nack(new RuntimeException()))
                                    .onItem().transform(ignored -> message);
                        }
                    }).collect().asList()
                    .await().atMost(Duration.ofSeconds(30));
            return received.stream().map(Message::getPayload).toList();
        }
    }

    private FetchConsumerConfiguration<Data> createFetchConsumerConfiguration(String subject) {
        return new FetchConsumerConfiguration<>() {
            @Override
            public Duration timeout() {
                return Duration.ofSeconds(3);
            }

            @Override
            public Integer batchSize() {
                return 10;
            }

            @Override
            public ConsumerConfiguration<Data> consumerConfiguration() {
                return createConsumerConfiguration(subject);
            }
        };
    }

    private ConsumerConfiguration<Data> createConsumerConfiguration(String subject) {
        return new ConsumerConfiguration<>() {

            @Override
            public String name() {
                return "fetch-consumer";
            }

            @Override
            public String stream() {
                return "fetch-test";
            }

            @Override
            public Optional<String> durable() {
                return Optional.of("fetch-consumer");
            }

            @Override
            public String subject() {
                return subject;
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
                return Optional.empty();
            }

            @Override
            public Optional<ReplayPolicy> replayPolicy() {
                return Optional.of(ReplayPolicy.Instant);
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

            @Override
            public Optional<AckPolicy> ackPolicy() {
                return Optional.of(AckPolicy.Explicit);
            }

            @Override
            public Optional<ZonedDateTime> pauseUntil() {
                return Optional.empty();
            }

            @Override
            public Optional<Class<Data>> payloadType() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> acknowledgeTimeout() {
                return Optional.of(Duration.ofMillis(1000));
            }
        };
    }

    private PublishConfiguration createPublishConfiguration(String subject) {
        return new PublishConfiguration() {

            @Override
            public String stream() {
                return "fetch-test";
            }

            @Override
            public String subject() {
                return subject;
            }
        };
    }

}
