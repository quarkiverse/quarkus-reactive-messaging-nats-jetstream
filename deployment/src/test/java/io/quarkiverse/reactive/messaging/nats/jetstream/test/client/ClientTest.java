package io.quarkiverse.reactive.messaging.nats.jetstream.test.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Multi;

public class ClientTest {
    private static final Logger log = Logger.getLogger(ClientTest.class);

    static final Duration TIMEOUT = Duration.ofSeconds(10);

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestSpanExporter.class, Data.class, ClientConsumerConfiguration.class,
                            ClientPullConfiguration.class, ClientPushConfiguration.class))
            .withConfigurationResource("application-client.properties");

    @Inject
    Client client;

    @Test
    void streamNames() {
        var streamNames = client.streamNames().collect().asList().await().atMost(TIMEOUT);
        assertThat(streamNames).contains("client-test");
    }

    @Test
    void subjects() {
        var subjects = client.subjects("client-test").collect().asList().await().atMost(TIMEOUT);
        assertThat(subjects).contains("client-data");
    }

    @Test
    void purge() {
        var result = client.purge("client-test").await().atMost(TIMEOUT);
        assertThat(result.success()).isTrue();
        assertThat(result.streamName()).isEqualTo("client-test");
    }

    @Test
    void firstSequence() {
        var firstSequence = client.firstSequence("client-test").await().atMost(TIMEOUT);
        assertThat(firstSequence).isNotNull();
    }

    @Test
    void deleteMessage() {
        var message = client.publish(Message.of(new Data("delete")), "client-test", "client-data").await().atMost(TIMEOUT);
        var metadata = message.getMetadata(PublishMessageMetadata.class);
        assertThat(metadata).isPresent();

        var sequence = metadata.get().sequence();
        assertThat(sequence).isNotNull();
        client.deleteMessage("client-test", sequence, true).await().atMost(TIMEOUT);
    }

    @Test
    void streamState() {
        var state = client.streamState("client-test").await().atMost(TIMEOUT);
        assertThat(state).isNotNull();
    }

    @Test
    void streamConfiguration() {
        var configuration = client.streamConfiguration("client-test").await().atMost(TIMEOUT);
        assertThat(configuration).isNotNull();
        assertThat(configuration.name()).isEqualTo("client-test");
    }

    @Test
    void purgeAll() {
        var result = client.purgeAll().collect().asList().await().atMost(TIMEOUT);
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void addAndRemoveSubject() {
        var subjects = client.subjects("client-test").collect().asList().await().atMost(TIMEOUT);
        assertThat(subjects).contains("client-data");
        assertThat(subjects).doesNotContain("client-temp");

        client.addSubject("client-test", "client-temp").await().atMost(TIMEOUT);
        subjects = client.subjects("client-test").collect().asList().await().atMost(TIMEOUT);
        assertThat(subjects).contains("client-data", "client-temp");

        client.removeSubject("client-test", "client-temp").await().atMost(TIMEOUT);
        subjects = client.subjects("client-test").collect().asList().await().atMost(TIMEOUT);
        assertThat(subjects).contains("client-data");
        assertThat(subjects).doesNotContain("client-temp");
    }

    @Test
    void keyValueCrudTest() {
        var data = new Data("c251274f-8528-4539-bc54-1b726cadd74e");

        var revision = client.putValue("client", "data", data).await().atMost(TIMEOUT);
        assertThat(revision).isGreaterThan(0L);

        var read = client.getValue("client", "data", Data.class).await().atMost(TIMEOUT);
        assertThat(read).isEqualTo(data);

        client.deleteValue("client", "data").await().atMost(TIMEOUT);
        read = client.getValue("client", "data", Data.class).await().atMost(TIMEOUT);
        assertThat(read).isNull();
    }

    @Test
    void publishAndConsumeMessagesWithPullSubscription() {
        var consumerConfiguration = new ClientConsumerConfiguration<Data>("client-test", "client-pull-subscription",
                List.of("client-data"));
        var pullConfiguration = new ClientPullConfiguration();

        client.addConsumerIfAbsent(consumerConfiguration, pullConfiguration).await().atMost(TIMEOUT);
        var consumers = client.consumerNames("client-test").collect().asList().await().atMost(TIMEOUT);
        assertThat(consumers).contains("client-pull-subscription");

        var messages = Multi.createFrom()
                .items(Stream.of(Message.of(new Data("1")), Message.of(new Data("2")), Message.of(new Data("3"))));
        var published = client.publish(messages, "client-test", "client-data").collect().asList().await().atMost(TIMEOUT);
        assertThat(published).hasSize(3);

        final var consumed = new ArrayList<Data>();
        client.subscribe(consumerConfiguration, pullConfiguration, Data.class)
                .onItem().invoke(message -> consumed.add(message.getPayload()))
                .subscribe().with(
                        item -> log.infof("Consumed message: %s", item.getPayload()),
                        log::error);

        Awaitility.await().atMost(TIMEOUT).until(() -> consumed.size() == 3);
    }

    @Test
    void publishAndConsumeMessagesWithPushSubscription() {
        var consumerConfiguration = new ClientConsumerConfiguration<Data>("client-test", "client-push-subscription",
                List.of("push-data"));
        var pushConfiguration = new ClientPushConfiguration("deliver-push-data");

        client.addConsumerIfAbsent(consumerConfiguration, pushConfiguration).await().atMost(TIMEOUT);
        var consumers = client.consumerNames("client-test").collect().asList().await().atMost(TIMEOUT);
        assertThat(consumers).contains("client-push-subscription");

        var messages = Multi.createFrom()
                .items(Stream.of(Message.of(new Data("1")), Message.of(new Data("2")), Message.of(new Data("3"))));
        var published = client.publish(messages, "client-test", "push-data").collect().asList().await().atMost(TIMEOUT);
        assertThat(published).hasSize(3);

        final var consumed = new ArrayList<Data>();
        client.subscribe(consumerConfiguration, pushConfiguration, Data.class)
                .onItem().invoke(message -> consumed.add(message.getPayload()))
                .subscribe().with(
                        item -> log.infof("Consumed message: %s", item.getPayload()),
                        log::error);

        Awaitility.await().atMost(TIMEOUT).until(() -> consumed.size() == 3);
    }

    @Test
    void addConsumerTwiceWithoutFailure() {
        var consumerConfiguration = new ClientConsumerConfiguration<Data>("client-test", "client-pull-subscription",
                List.of("client-data"));
        var pullConfiguration = new ClientPullConfiguration();

        client.addConsumerIfAbsent(consumerConfiguration, pullConfiguration).await().atMost(TIMEOUT);
        var consumers = client.consumerNames("client-test").collect().asList().await().atMost(TIMEOUT);
        assertThat(consumers).contains("client-pull-subscription");

        client.addConsumerIfAbsent(consumerConfiguration, pullConfiguration).await().atMost(TIMEOUT);
        consumers = client.consumerNames("client-test").collect().asList().await().atMost(TIMEOUT);
        assertThat(consumers).contains("client-pull-subscription");
    }
}
