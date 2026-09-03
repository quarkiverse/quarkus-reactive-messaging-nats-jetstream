package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamContainer;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamContainerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.AcknowledgeMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.DisabledTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;

@Testcontainers
public class ClientTest {
    static final Logger log = Logger.getLogger(ClientTest.class);
    static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Container
    static JetStreamContainer jetStreamContainer = new JetStreamContainer(null, null, false,
            JetStreamContainerConfiguration.of("test", "test", false, null, null));

    private ExecutorService executorService;
    private Client client;

    @SuppressWarnings("resource")
    @BeforeAll
    static void initLogging() {
        var handler = new org.jboss.logmanager.handlers.ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(
                new org.jboss.logmanager.formatters.PatternFormatter("%d{HH:mm:ss,SSS} %-5p [%c{1.}] (%t) %s%e%n"));
        io.quarkus.bootstrap.logging.InitialConfigurator.DELAYED_HANDLER
                .setHandlers(new java.util.logging.Handler[] { handler });
    }

    @BeforeEach
    void setup() {
        executorService = Executors.newFixedThreadPool(4);
        final var clientFactory = new ExecutorClientFactory(new DisabledTracerFactory());
        client = clientFactory.create(new ClientConnectionConfiguration(jetStreamContainer), new ClientSerializer(),
                executorService);

        client.streamManagement().addIfAbsent(new ClientStreamConfiguration("client-test", Set.of("client-data", "push-data")))
                .await().atMost(TIMEOUT);
        client.keyValueManagement().addIfAbsent(new ClientKeyValueConfiguration("client", Optional.of("Client Bucket"))).await()
                .atMost(TIMEOUT);
    }

    @AfterEach
    void teardown() throws Exception {
        if (client != null) {
            client.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Test
    void streamNames() {
        var streamNames = client.streamManagement().streams().map(streamInfo -> streamInfo.configuration().name()).collect()
                .asList().await().atMost(TIMEOUT);
        assertThat(streamNames).contains("client-test");
    }

    @Test
    void subjects() {
        var subjects = client.streamManagement().stream("client-test").map(streamInfo -> streamInfo.configuration().subjects())
                .await().atMost(TIMEOUT);
        assertThat(subjects).contains("client-data");
    }

    @Test
    void purge() {
        var result = client.streamManagement().purge("client-test").await().atMost(TIMEOUT);
        assertThat(result.success()).isTrue();
        assertThat(result.stream()).isEqualTo("client-test");
    }

    @Test
    void deleteMessage() {
        var message = client.publish(Message.of(new Data("delete")), "client-test", "client-data")
                .await().atMost(TIMEOUT);
        var metadata = message.getMetadata(AcknowledgeMetadata.class);
        assertThat(metadata).isPresent();

        var sequence = metadata.get().sequenceNumber();
        assertThat(sequence).isGreaterThan(0);

        client.streamManagement().deleteMessage("client-test", sequence, true).await().atMost(TIMEOUT);
    }

    @Test
    void streamInfo() {
        var streamInfo = client.streamManagement().stream("client-test").await().atMost(TIMEOUT);
        assertThat(streamInfo).isNotNull();
        assertThat(streamInfo.configuration()).isNotNull();
        assertThat(streamInfo.configuration().name()).isEqualTo("client-test");
    }

    @Test
    void purgeAll() {
        var result = client.streamManagement().purgeAll().collect().asList().await().atMost(TIMEOUT);
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void addAndRemoveSubject() {
        var subjects = client.streamManagement().stream("client-test").map(Stream::configuration)
                .map(StreamConfiguration::subjects).await().atMost(TIMEOUT);
        assertThat(subjects).contains("client-data");
        assertThat(subjects).doesNotContain("client-temp");

        client.streamManagement().addSubject("client-test", "client-temp").await().atMost(TIMEOUT);
        subjects = client.streamManagement().stream("client-test").map(Stream::configuration)
                .map(StreamConfiguration::subjects).await().atMost(TIMEOUT);
        assertThat(subjects).contains("client-data", "client-temp");

        client.streamManagement().removeSubject("client-test", "client-temp").await().atMost(TIMEOUT);
        subjects = client.streamManagement().stream("client-test").map(Stream::configuration)
                .map(StreamConfiguration::subjects).await().atMost(TIMEOUT);
        assertThat(subjects).contains("client-data");
        assertThat(subjects).doesNotContain("client-temp");
    }

    @Test
    void keyValueCrudTest() throws Exception {
        var data = new Data("c251274f-8528-4539-bc54-1b726cadd74e");

        final var keyValue = client.keyValue("client");

        final var serializer = new ClientSerializer();
        var entry = keyValue.put("data", serializer.toBytes(data)).await().atMost(TIMEOUT);
        assertThat(entry.revision()).isGreaterThan(0L);

        final var objectMapper = new ObjectMapper();
        var read = objectMapper.readValue(
                keyValue.get("data").await().atMost(TIMEOUT).value().orElseThrow(() -> new RuntimeException("Value not found")),
                Data.class);
        assertThat(read).isEqualTo(data);

        keyValue.delete("data").await().atMost(TIMEOUT);
        entry = keyValue.get("data").await().atMost(TIMEOUT);
        assertThat(entry).isNull();
    }

    @Test
    void publishAndConsumeMessages() {
        var consumerConfiguration = new ClientConsumerConfiguration("client-pull-subscription", Set.of("client-data"));

        client.consumerManagement("client-test").addIfAbsent(consumerConfiguration).await().atMost(TIMEOUT);
        var consumers = client.consumerManagement("client-test").consumers().map(Consumer::name).collect().asList().await()
                .atMost(TIMEOUT);
        assertThat(consumers).contains("client-pull-subscription");

        var messages = List.of(Message.of(new Data("1")),
                Message.of(new Data("2")),
                Message.of(new Data("3")));

        messages.forEach(message -> client.publish(message, "client-test", "client-data").await().atMost(TIMEOUT));

        final var consumed = new ArrayList<Message<Data>>();
        client.<Data> subscribe("client-test", "client-pull-subscription", TIMEOUT, 3)
                .onItem().invoke(consumed::add)
                .onFailure().invoke(log::error)
                .subscribe().with(
                        item -> log.infof("Consumed message: %s", item.getPayload()),
                        log::error);

        Awaitility.await().atMost(TIMEOUT).until(() -> consumed.size() == 3);
    }

    @Test
    void addConsumerTwiceWithoutFailure() {
        var consumerConfiguration = new ClientConsumerConfiguration("client-pull-subscription", Set.of("client-data"));

        client.consumerManagement("client-test").addIfAbsent(consumerConfiguration).await().atMost(TIMEOUT);
        var consumers = client.consumerManagement("client-test").consumers().map(Consumer::name).collect().asList().await()
                .atMost(TIMEOUT);
        assertThat(consumers).contains("client-pull-subscription");

        client.consumerManagement("client-test").addIfAbsent(consumerConfiguration).await().atMost(TIMEOUT);
        consumers = client.consumerManagement("client-test").consumers().map(Consumer::name).collect().asList().await()
                .atMost(TIMEOUT);
        assertThat(consumers).contains("client-pull-subscription");
    }
}
