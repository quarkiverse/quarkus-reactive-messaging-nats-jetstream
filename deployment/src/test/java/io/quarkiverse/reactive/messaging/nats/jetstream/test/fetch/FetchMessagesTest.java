package io.quarkiverse.reactive.messaging.nats.jetstream.test.fetch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.NackMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.MessageConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusUnitTest;

public class FetchMessagesTest implements MessageConsumer<Object> {
    private final static Duration TIMEOUT = Duration.ofSeconds(5);

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addClasses(TestSpanExporter.class, Data.class, ConsumerConfiguration.class,
                                    FetchConfiguration.class, MessageConsumer.class))
            .withConfigurationResource("application-fetch.properties");

    @Inject
    Client client;

    @BeforeEach
    public void setup() {
        client.purgeAll().collect().asList().await().atMost(Duration.ofSeconds(30));
    }

    @Test
    void fetchOneMessage() {
        final var data = new Data("test", "52b13992-749a-4943-ab8f-2403c734c648", "46c818c9-8915-48a6-9378-b8f540b0afe2");

        final var configuration = new ConsumerConfiguration("fetch-test", "fetch-data-consumer", "fetch-data");
        client.addConsumerIfAbsent(configuration).await().atMost(TIMEOUT);

        client.publish(Message.of(data), "fetch-test", "fetch-data").await().atMost(TIMEOUT);

        final var received = client.next(configuration, TIMEOUT).await().atMost(TIMEOUT);
        acknowledge(received).await().atMost(TIMEOUT);

        assertThat(received.getPayload()).isEqualTo(data);
    }

    @Test
    void fetchTwoMessages() {
        final var data1 = new Data("test1", "ea030796-4692-40f1-9ce5-a9cf04b5fe53", "3bd00e71-7bc3-45c3-a1aa-8f8679ff7373");
        final var data2 = new Data("test2", "4d76e337-97f8-41b9-9030-b19d4ba824be", "58707f28-74c5-45fd-b59a-be0286bb8490");

        final var configuration = new ConsumerConfiguration("fetch-test", "fetch-data-consumer", "fetch-data");
        client.addConsumerIfAbsent(configuration).await().atMost(TIMEOUT);

        client.publish(Message.of(data1), "fetch-test", "fetch-data").await().atMost(TIMEOUT);
        client.publish(Message.of(data2), "fetch-test", "fetch-data").await().atMost(TIMEOUT);

        final var received1 = client.next(configuration, TIMEOUT).await().atMost(TIMEOUT);
        acknowledge(received1).await().atMost(TIMEOUT);
        assertThat(received1.getPayload()).isEqualTo(data1);

        final var received2 = client.next(configuration, TIMEOUT).await().atMost(TIMEOUT);
        acknowledge(received2).await().atMost(TIMEOUT);
        assertThat(received2.getPayload()).isEqualTo(data2);
    }

    @Test
    void fetchOneNotAcknowledgedMessage() {
        final var data1 = new Data("test1", "ea030796-4692-40f1-9ce5-a9cf04b5fe53", "3bd00e71-7bc3-45c3-a1aa-8f8679ff7373");
        final var data2 = new Data("test2", "4d76e337-97f8-41b9-9030-b19d4ba824be", "58707f28-74c5-45fd-b59a-be0286bb8490");

        final var configuration = new ConsumerConfiguration("fetch-test", "fetch-data-consumer", "fetch-data");
        client.addConsumerIfAbsent(configuration).await().atMost(TIMEOUT);

        client.publish(Message.of(data1), "fetch-test", "fetch-data").await().atMost(TIMEOUT);
        client.publish(Message.of(data2), "fetch-test", "fetch-data").await().atMost(TIMEOUT);

        final var received1 = client.next(configuration, TIMEOUT).await().atMost(TIMEOUT);
        assertThat(received1).isNotNull();
        notAcknowledge(received1, new RuntimeException()).await().atMost(TIMEOUT);
        assertThat(received1.getPayload()).isEqualTo(data1);

        final var received2 = client.next(configuration, TIMEOUT).await().atMost(TIMEOUT);
        assertThat(received2).isNotNull();
        acknowledge(received2).await().atMost(TIMEOUT);
        assertThat(received2.getPayload()).isEqualTo(data1);
    }

    @Test
    void fetchOneNotAcknowledgedWithDelayMessage() {
        final var data1 = new Data("test1", "ea030796-4692-40f1-9ce5-a9cf04b5fe53", "3bd00e71-7bc3-45c3-a1aa-8f8679ff7373");

        final var configuration = new ConsumerConfiguration("fetch-test", "fetch-data-consumer", "fetch-data");
        client.addConsumerIfAbsent(configuration).await().atMost(TIMEOUT);

        client.publish(Message.of(data1), "fetch-test", "fetch-data").await().atMost(TIMEOUT);

        final var received1 = client.next(configuration, TIMEOUT).await().atMost(TIMEOUT);
        assertThat(received1).isNotNull();
        assertThat(received1.getPayload()).isEqualTo(data1);
        notAcknowledge(received1, new RuntimeException(),
                Metadata.of(NackMetadata.builder().delayWait(TIMEOUT.plusSeconds(5)).build())).await().atMost(TIMEOUT);

        // The message should not be available immediately after a nack with delay
        assertThat(client.next(configuration, TIMEOUT).await().atMost(TIMEOUT.plusSeconds(1))).isNull();

        // Attempt to fetch the message until it is available again
        final var received2 = client.next(configuration, Duration.ofSeconds(35L)).await().atMost(Duration.ofSeconds(35L));
        assertThat(received2).isNotNull();
        assertThat(received2.getPayload()).isEqualTo(data1);
    }

    @Test
    void subjectTokens() {
        final var data1 = new Data("test1", "14e9aaaf-0d42-42a8-a93a-ebe37ff6a742", "974932c1-90b8-4b79-b10a-b508d7badc04");
        final var data2 = new Data("test2", "8a6faacc-f05e-44f1-bc4b-70abbd5a1b50", "057e17c7-55ad-4970-841a-ad712048e0e1");
        final var data3 = new Data("test3", "14e9aaaf-0d42-42a8-a93a-ebe37ff6a742", "974932c1-90b8-4b79-b10a-b508d7badc04");
        final var data4 = new Data("test4", "5246354a-2342-4422-9268-af95862b51fb", "1325e196-4186-47ab-8b30-5047cae77d7e");

        final var configuration1 = new ConsumerConfiguration("fetch-test", data1.resourceId(),
                "resources." + data1.resourceId());
        final var configuration2 = new ConsumerConfiguration("fetch-test", data2.resourceId(),
                "resources." + data2.resourceId());
        final var configuration3 = new ConsumerConfiguration("fetch-test", data3.resourceId(),
                "resources." + data3.resourceId());
        final var configuration4 = new ConsumerConfiguration("fetch-test", data4.resourceId(),
                "resources." + data4.resourceId());

        client.addConsumerIfAbsent(configuration1).await().atMost(TIMEOUT);
        client.addConsumerIfAbsent(configuration2).await().atMost(TIMEOUT);
        client.addConsumerIfAbsent(configuration3).await().atMost(TIMEOUT);
        client.addConsumerIfAbsent(configuration4).await().atMost(TIMEOUT);

        client.publish(Message.of(data1), "fetch-test", "resources." + data1.resourceId()).await().atMost(TIMEOUT);
        client.publish(Message.of(data2), "fetch-test", "resources." + data2.resourceId()).await().atMost(TIMEOUT);
        client.publish(Message.of(data3), "fetch-test", "resources." + data3.resourceId()).await().atMost(TIMEOUT);
        client.publish(Message.of(data4), "fetch-test", "resources." + data4.resourceId()).await().atMost(TIMEOUT);

        final var received1 = client.next(configuration1, TIMEOUT).await().atMost(TIMEOUT);
        acknowledge(received1).await().atMost(TIMEOUT);
        assertThat(received1.getPayload()).isEqualTo(data1);

        final var received2 = client.next(configuration2, TIMEOUT).await().atMost(TIMEOUT);
        acknowledge(received2).await().atMost(TIMEOUT);
        assertThat(received2.getPayload()).isEqualTo(data2);

        final var received3 = client.next(configuration3, TIMEOUT).await().atMost(TIMEOUT);
        acknowledge(received3).await().atMost(TIMEOUT);
        assertThat(received3.getPayload()).isEqualTo(data3);

        final var received4 = client.next(configuration4, TIMEOUT).await().atMost(TIMEOUT);
        acknowledge(received4).await().atMost(TIMEOUT);
        assertThat(received4.getPayload()).isEqualTo(data4);
    }

    @Test
    void addAndRemoveSubject() {
        final var data1 = new Data("test1", "64a8903f-983a-4775-8c41-e59c1a40ca08", "5a6af883-2be2-4c73-9d5d-7cdc4157f2fb");
        final var data2 = new Data("test2", "7a229cc2-e8e4-4a59-ba0a-40e878c9b3af", "d38ddb6f-3b9c-4a6c-978e-e97c0b66a2fd");

        client.addSubject("fetch-test", data1.resourceId()).await().atMost(TIMEOUT);
        client.addSubject("fetch-test", data2.resourceId()).await().atMost(TIMEOUT);

        final var configuration1 = new ConsumerConfiguration("fetch-test", data1.resourceId(), data1.resourceId());
        final var configuration2 = new ConsumerConfiguration("fetch-test", data2.resourceId(), data2.resourceId());

        client.addConsumerIfAbsent(configuration1).await().atMost(TIMEOUT);
        client.addConsumerIfAbsent(configuration2).await().atMost(TIMEOUT);

        client.publish(Message.of(data1), "fetch-test", data1.resourceId()).await().atMost(TIMEOUT);
        client.publish(Message.of(data2), "fetch-test", data2.resourceId()).await().atMost(TIMEOUT);

        final var received1 = client.next(configuration1, TIMEOUT).await().atMost(TIMEOUT);
        acknowledge(received1).await().atMost(TIMEOUT);
        assertThat(received1.getPayload()).isEqualTo(data1);

        final var received2 = client.next(configuration2, TIMEOUT).await().atMost(TIMEOUT);
        acknowledge(received2).await().atMost(TIMEOUT);
        assertThat(received2.getPayload()).isEqualTo(data2);

        client.removeSubject("fetch-test", data1.resourceId()).await().atMost(TIMEOUT);
        client.removeSubject("fetch-test", data2.resourceId()).await().atMost(TIMEOUT);
    }

    @Test
    void fetchMessages() throws Exception {
        final var data1 = new Data("test1", "64a8903f-983a-4775-8c41-e59c1a40ca08", "5a6af883-2be2-4c73-9d5d-7cdc4157f2fb");
        final var data2 = new Data("test2", "64a8903f-983a-4775-8c41-e59c1a40ca08", "d38ddb6f-3b9c-4a6c-978e-e97c0b66a2fd");

        final var consumer = "d28ed8ca-fa92-4b03-9882-713ab696c648";
        final var subject = "37eb6a5b-9b30-4b95-9ef7-fb866d18ef50";

        client.addSubject("fetch-test", subject).await().atMost(TIMEOUT);

        final var configuration = new ConsumerConfiguration("fetch-test", consumer, subject);
        client.addConsumerIfAbsent(configuration).await().atMost(TIMEOUT);

        client.publish(Message.of(data1), "fetch-test", subject).await().atMost(TIMEOUT);
        client.publish(Message.of(data2), "fetch-test", subject).await().atMost(TIMEOUT);

        final var received = client.fetch(configuration, new FetchConfiguration())
                .onItem()
                .transformToUniAndMerge(message -> acknowledge(message).onItem().transform(ignored -> message.getPayload()))
                .collect().asList()
                .await().atMost(TIMEOUT);

        assertThat(received).containsExactly(data1, data2);
    }
}
