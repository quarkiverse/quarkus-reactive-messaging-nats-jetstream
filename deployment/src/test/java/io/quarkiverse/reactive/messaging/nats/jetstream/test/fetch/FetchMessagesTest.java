package io.quarkiverse.reactive.messaging.nats.jetstream.test.fetch;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.NackMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FetchMessagesTest {

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addClasses(TestSpanExporter.class, Data.class))
            .withConfigurationResource("application-fetch.properties");

    @Inject
    Client client;

    @BeforeEach
    public void setup() {
        client.purgeAll().collect().asList().await().atMost(Duration.ofSeconds(30));
    }

    @Test
    void fetchOneMessage() throws Exception {
        final var data = new Data("test", "52b13992-749a-4943-ab8f-2403c734c648", "46c818c9-8915-48a6-9378-b8f540b0afe2");

        final var consumerConfiguration = new ConsumerConfiguration("fetch-test", "fetch-data-consumer", "fetch-data");
        addConsumer(consumerConfiguration);

        publish(data, "fetch-data");

        final var received = next("fetch-data-consumer", "fetch-data", true);
        assertThat(received).isEqualTo(data);
    }

    @Test
    void fetchTwoMessages() throws Exception {
        final var data1 = new Data("test1", "ea030796-4692-40f1-9ce5-a9cf04b5fe53", "3bd00e71-7bc3-45c3-a1aa-8f8679ff7373");
        final var data2 = new Data("test2", "4d76e337-97f8-41b9-9030-b19d4ba824be", "58707f28-74c5-45fd-b59a-be0286bb8490");

        final var consumerConfiguration = new ConsumerConfiguration("fetch-test", "fetch-data-consumer", "fetch-data");
        addConsumer(consumerConfiguration);

        publish(data1, "fetch-data");
        publish(data2, "fetch-data");

        final var received1 = next("fetch-data-consumer", "fetch-data", true);
        assertThat(received1).isEqualTo(data1);

        final var received2 = next("fetch-data-consumer", "fetch-data", true);
        assertThat(received2).isEqualTo(data2);
    }

    @Test
    void fetchOneNotAcknowledgedMessage() throws Exception {
        final var data1 = new Data("test1", "ea030796-4692-40f1-9ce5-a9cf04b5fe53", "3bd00e71-7bc3-45c3-a1aa-8f8679ff7373");
        final var data2 = new Data("test2", "4d76e337-97f8-41b9-9030-b19d4ba824be", "58707f28-74c5-45fd-b59a-be0286bb8490");

        final var consumerConfiguration = new ConsumerConfiguration("fetch-test", "fetch-data-consumer", "fetch-data");
        addConsumer(consumerConfiguration);

        publish(data1, "fetch-data");
        publish(data2, "fetch-data");

        final var received1 = next("fetch-data-consumer", "fetch-data", false);
        assertThat(received1).isEqualTo(data1);

        final var received2 = next("fetch-data-consumer", "fetch-data", true);
        assertThat(received2).isEqualTo(data1);
    }

    @Test
    void fetchOneNotAcknowledgedWithDelayMessage() throws Exception {
        final var data1 = new Data("test1", "ea030796-4692-40f1-9ce5-a9cf04b5fe53", "3bd00e71-7bc3-45c3-a1aa-8f8679ff7373");

        final var consumerConfiguration = new ConsumerConfiguration("fetch-test", "fetch-data-consumer", "fetch-data");
        addConsumer(consumerConfiguration);

        publish(data1, "fetch-data");

        final var received1 = next("fetch-data-consumer", "fetch-data", false, Duration.ofSeconds(35L));
        assertThat(received1).isEqualTo(data1);

        // The message should not be available immediately after a nack with delay
        assertThat(next("fetch-data-consumer", "fetch-data", true)).isNull();

        // Attempt to fetch the message until it is available again
        final var received2 = next("fetch-data-consumer", "fetch-data", true);
        assertThat(received2).isEqualTo(data1);
    }

    @Test
    void subjectTokens() throws Exception {
        final var data1 = new Data("test1", "14e9aaaf-0d42-42a8-a93a-ebe37ff6a742", "974932c1-90b8-4b79-b10a-b508d7badc04");
        final var data2 = new Data("test2", "8a6faacc-f05e-44f1-bc4b-70abbd5a1b50", "057e17c7-55ad-4970-841a-ad712048e0e1");
        final var data3 = new Data("test3", "14e9aaaf-0d42-42a8-a93a-ebe37ff6a742", "974932c1-90b8-4b79-b10a-b508d7badc04");
        final var data4 = new Data("test4", "5246354a-2342-4422-9268-af95862b51fb", "1325e196-4186-47ab-8b30-5047cae77d7e");

        addConsumer(new ConsumerConfiguration("fetch-test", data1.resourceId(), "resources." + data1.resourceId()));
        addConsumer(new ConsumerConfiguration("fetch-test", data2.resourceId(), "resources." + data2.resourceId()));
        addConsumer(new ConsumerConfiguration("fetch-test", data3.resourceId(), "resources." + data3.resourceId()));
        addConsumer(new ConsumerConfiguration("fetch-test", data4.resourceId(), "resources." + data4.resourceId()));

        publish(data1, "resources." + data1.resourceId());
        publish(data2, "resources." + data2.resourceId());
        publish(data3, "resources." + data3.resourceId());
        publish(data4, "resources." + data4.resourceId());

        final var received1 = next(data1.resourceId(), "resources." + data1.resourceId(), true);
        assertThat(received1).isEqualTo(data1);

        final var received2 = next(data2.resourceId(), "resources." + data2.resourceId(), true);
        assertThat(received2).isEqualTo(data2);

        final var received3 = next(data3.resourceId(), "resources." + data3.resourceId(), true);
        assertThat(received3).isEqualTo(data3);

        final var received4 = next(data4.resourceId(), "resources." + data4.resourceId(), true);
        assertThat(received4).isEqualTo(data4);
    }

    @Test
    void addAndRemoveSubject() throws Exception {
        final var data1 = new Data("test1", "64a8903f-983a-4775-8c41-e59c1a40ca08", "5a6af883-2be2-4c73-9d5d-7cdc4157f2fb");
        final var data2 = new Data("test2", "7a229cc2-e8e4-4a59-ba0a-40e878c9b3af", "d38ddb6f-3b9c-4a6c-978e-e97c0b66a2fd");

        addSubject(data1.resourceId());
        addSubject(data2.resourceId());

        addConsumer(new ConsumerConfiguration("fetch-test", data1.resourceId(), data1.resourceId()));
        addConsumer(new ConsumerConfiguration("fetch-test", data2.resourceId(), data2.resourceId()));

        publish(data1, data1.resourceId());
        publish(data2, data2.resourceId());

        final var received1 = next(data1.resourceId(), data1.resourceId(), true);
        assertThat(received1).isEqualTo(data1);

        final var received2 = next(data2.resourceId(), data2.resourceId(), true);
        assertThat(received2).isEqualTo(data2);

        removeSubject(data1.resourceId());
        removeSubject(data2.resourceId());
    }

    @Test
    void fetchMessages() throws Exception {
        final var data1 = new Data("test1", "64a8903f-983a-4775-8c41-e59c1a40ca08", "5a6af883-2be2-4c73-9d5d-7cdc4157f2fb");
        final var data2 = new Data("test2", "64a8903f-983a-4775-8c41-e59c1a40ca08", "d38ddb6f-3b9c-4a6c-978e-e97c0b66a2fd");

        addSubject(data1.resourceId());

        addConsumer(new ConsumerConfiguration("fetch-test", data1.resourceId(), data1.resourceId()));

        publish(data1, data1.resourceId());
        publish(data2, data2.resourceId());

        final var received = fetch(data1.resourceId(), data1.resourceId());
        assertThat(received).containsExactly(data1, data2);
    }

    private void addSubject(String subject) {
        client.addSubject("fetch-test", subject).await().atMost(Duration.ofSeconds(30));
    }

    private void addConsumer(ConsumerConfiguration configuration) {
        client.addConsumerIfAbsent(configuration).await().atMost(Duration.ofSeconds(30));
    }

    private void removeSubject(String subject) {
        client.removeSubject("fetch-test", subject).await().atMost(Duration.ofSeconds(30));
    }

    private void publish(Data data, String subject) {
        client.publish(Message.of(data), "fetch-test", subject)
                .await()
                .atMost(Duration.ofSeconds(30));
    }

    private Data next(String consumer, String subject, boolean ack) {
        return next(consumer, subject, ack, Duration.ofSeconds(30));
    }

    private Data next(String consumer, String subject, boolean ack, @Nullable Duration waitDelay) {
        final var consumerConfiguration = new ConsumerConfiguration("fetch-test", consumer, subject);
        final var received = client.next(consumerConfiguration, Duration.ofSeconds(30))
                .await().atMost(Duration.ofSeconds(30));
        if (ack) {
            Uni.createFrom().completionStage(received.ack()).await().atMost(Duration.ofSeconds(30));
        } else {
            if (waitDelay != null) {
                Uni.createFrom()
                        .completionStage(received.nack(new RuntimeException(),
                                Metadata.of(NackMetadata.builder().delayWait(waitDelay).build())))
                        .await().atMost(Duration.ofSeconds(30));
            } else {
                Uni.createFrom().completionStage(received.nack(new RuntimeException())).await()
                        .atMost(Duration.ofSeconds(30));
            }
        }
        return (Data) received.getPayload();
    }

    private List<Data> fetch(String consumer, String subject) throws Exception {
        final var consumerConfiguration = new ConsumerConfiguration("fetch-test", consumer, subject);
        final var received = client.fetch(consumerConfiguration, new FetchConfiguration())
                .onItem().transformToUniAndMerge(message -> Uni.createFrom().completionStage(message.ack())
                        .onItem().transform(ignored -> message))
                .collect().asList()
                .await().atMost(Duration.ofSeconds(30));
        return received.stream().map(Message::getPayload).map(payload -> (Data) payload).toList();
    }
}
