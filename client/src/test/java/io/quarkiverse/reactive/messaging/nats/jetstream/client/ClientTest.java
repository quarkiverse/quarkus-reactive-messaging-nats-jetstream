package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
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

import io.nats.client.MessageTtl;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamContainer;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamContainerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.AcknowledgeMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.DisabledTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;

@Testcontainers
public class ClientTest {
    static final Logger log = Logger.getLogger(ClientTest.class);
    static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Container
    static final JetStreamContainer jetStreamContainer = new JetStreamContainer(null, null, false,
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
        client.keyValueManagement()
                .addIfAbsent(new ClientKeyValueConfiguration("client-ttl", Optional.of("Client TTL Bucket"),
                        Optional.of(Duration.ofSeconds(1)), 64))
                .await().atMost(TIMEOUT);
        client.keyValueManagement()
                .addIfAbsent(new ClientKeyValueConfiguration("client-ttl-latest", Optional.of("Client TTL Latest Bucket"),
                        Optional.of(Duration.ofSeconds(1)), 1))
                .await().atMost(TIMEOUT);
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
    void keyValueCrudTest() {
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
    void keyValuePutWithTtlOverwritesAndExpires() {
        final var keyValue = client.keyValue("client-ttl-latest");

        var first = keyValue.put("put-ttl", "first".getBytes(StandardCharsets.UTF_8)).await().atMost(TIMEOUT);
        var second = keyValue.put("put-ttl", "second".getBytes(StandardCharsets.UTF_8), MessageTtl.seconds(1)).await()
                .atMost(TIMEOUT);
        assertThat(second.revision()).isGreaterThan(first.revision());
        assertThat(second.value().map(String::new)).hasValue("second");
        assertThat(keyValue.get("put-ttl").await().atMost(TIMEOUT).value().map(String::new)).hasValue("second");

        Awaitility.await().atMost(TIMEOUT)
                .until(() -> keyValue.get("put-ttl").await().atMost(TIMEOUT) == null);
    }

    @Test
    void keyValuePutWithTtlExpiresToPreviousRevisionWhenHistoryIsKept() {
        final var keyValue = client.keyValue("client-ttl");

        keyValue.put("put-ttl-history", "first".getBytes(StandardCharsets.UTF_8)).await().atMost(TIMEOUT);
        keyValue.put("put-ttl-history", "second".getBytes(StandardCharsets.UTF_8), MessageTtl.seconds(1)).await()
                .atMost(TIMEOUT);

        // the TTL only removes the revision it was written with
        Awaitility.await().atMost(TIMEOUT)
                .until(() -> keyValue.get("put-ttl-history").await().atMost(TIMEOUT).value().map(String::new)
                        .filter("first"::equals).isPresent());
    }

    @Test
    void keyValuePutWithDurationTtlExpires() {
        final var keyValue = client.keyValue("client-ttl");

        var entry = keyValue.put("put-duration-ttl", "value".getBytes(StandardCharsets.UTF_8), Duration.ofMillis(500))
                .await().atMost(TIMEOUT);
        assertThat(entry.revision()).isGreaterThan(0L);

        Awaitility.await().atMost(TIMEOUT)
                .until(() -> keyValue.get("put-duration-ttl").await().atMost(TIMEOUT) == null);
    }

    @Test
    void keyValueCreateWithTtl() {
        final var keyValue = client.keyValue("client-ttl");

        var entry = keyValue.create("create-ttl", "value".getBytes(StandardCharsets.UTF_8), MessageTtl.seconds(1)).await()
                .atMost(TIMEOUT);
        assertThat(entry.revision()).isGreaterThan(0L);
        assertThat(entry.value().map(String::new)).hasValue("value");

        assertThatThrownBy(() -> keyValue.create("create-ttl", "other".getBytes(StandardCharsets.UTF_8),
                MessageTtl.seconds(1)).await().atMost(TIMEOUT))
                .isInstanceOf(KeyValueException.class);

        Awaitility.await().atMost(TIMEOUT)
                .until(() -> keyValue.get("create-ttl").await().atMost(TIMEOUT) == null);
        // once expired, the key can be created again
        entry = keyValue.create("create-ttl", "again".getBytes(StandardCharsets.UTF_8), MessageTtl.seconds(1)).await()
                .atMost(TIMEOUT);
        assertThat(entry.value().map(String::new)).hasValue("again");
    }

    @Test
    void keyValuePurgeWithTtl() throws Exception {
        final var keyValue = client.keyValue("client-ttl");

        keyValue.put("purge-ttl", "value".getBytes(StandardCharsets.UTF_8)).await().atMost(TIMEOUT);
        keyValue.purge("purge-ttl", MessageTtl.seconds(1)).await().atMost(TIMEOUT);
        assertThat(keyValue.get("purge-ttl").await().atMost(TIMEOUT)).isNull();

        // the purge marker itself expires, leaving no history for the key
        final var nativeKeyValue = client.nativeConnection().keyValue("client-ttl");
        Awaitility.await().atMost(TIMEOUT).until(() -> nativeKeyValue.history("purge-ttl").isEmpty());
    }

    @Test
    void keyValuePutWithTtlRejectedWithoutMessageTtlSupport() {
        final var keyValue = client.keyValue("client");

        assertThatThrownBy(() -> keyValue.put("no-ttl", "value".getBytes(StandardCharsets.UTF_8), MessageTtl.seconds(1))
                .await().atMost(TIMEOUT))
                .isInstanceOf(KeyValueException.class);
    }

    @Test
    void keyValuePutWithTtlOnMirrorWritesToOrigin() throws Exception {
        final var keyValueManagement = client.nativeConnection().keyValueManagement();
        keyValueManagement.create(io.nats.client.api.KeyValueConfiguration.builder()
                .name("client-ttl-mirror")
                .mirror(io.nats.client.api.Mirror.builder().sourceName("client-ttl").build())
                .build());
        try {
            client.keyValue("client-ttl-mirror")
                    .put("mirror-ttl", "value".getBytes(StandardCharsets.UTF_8), MessageTtl.seconds(5))
                    .await().atMost(TIMEOUT);

            var origin = client.keyValue("client-ttl").get("mirror-ttl").await().atMost(TIMEOUT);
            assertThat(origin).isNotNull();
            assertThat(origin.value().map(String::new)).hasValue("value");
        } finally {
            keyValueManagement.delete("client-ttl-mirror");
        }
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
