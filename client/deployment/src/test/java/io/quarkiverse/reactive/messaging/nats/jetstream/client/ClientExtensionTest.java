package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import jakarta.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.AcknowledgeMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Metadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.PublishHeaders;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;
import io.quarkus.test.QuarkusExtensionTest;
import io.smallrye.mutiny.Uni;
import tools.jackson.databind.ObjectMapper;

/**
 * Runs the client extension on its own, without Reactive Messaging: the {@link Client} bean connects to the Dev
 * Services broker and the stream and consumer configured for it exist on startup.
 */
public class ClientExtensionTest {
    private static final Logger log = Logger.getLogger(ClientExtensionTest.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class).addClasses(Data.class, ClientConsumerConfiguration.class))
            .withConfigurationResource("application-client.properties");

    @Inject
    Client client;

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

    @Test
    void reactiveMessagingIsNotOnTheClasspath() {
        assertThatThrownBy(() -> Class.forName("org.eclipse.microprofile.reactive.messaging.Message"))
                .isInstanceOf(ClassNotFoundException.class);
        assertThatThrownBy(() -> Class.forName("io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    void publishAndReceive() {
        final var published = client.publish(Message.of("hello"), "client-test", "client-test.data")
                .await().atMost(TIMEOUT);
        final var acknowledgement = published.getMetadata(AcknowledgeMetadata.class).orElseThrow();
        assertThat(acknowledgement.stream()).isEqualTo("client-test");

        final var received = client.next("client-test", "client-consumer", TIMEOUT, String.class)
                .await().atMost(TIMEOUT);
        assertThat(received.getPayload()).isEqualTo("hello");
        assertThat(received.getMetadata(MessageMetadata.class).orElseThrow().streamSequence())
                .isEqualTo(acknowledgement.sequenceNumber());
        Uni.createFrom().completionStage(received.ack()).await().atMost(TIMEOUT);
    }

    @Test
    void publishToDynamicSubjectMatchingWildcardSubject() {
        final var message = messageTo("account.42.orders.created");
        final var published = client.publish(message, "client-orders", "account.*.orders.>").await().atMost(TIMEOUT);

        assertThat(published.getMetadata(AcknowledgeMetadata.class).orElseThrow().stream()).isEqualTo("client-orders");

        final var other = messageTo("account.43.orders.created");
        assertThat(client.publish(other, "client-orders", "account.*.orders.>").await().atMost(TIMEOUT)
                .getMetadata(AcknowledgeMetadata.class)).isPresent();
    }

    @Test
    void publishToSubjectNotMatchingWildcardSubjectIsNotAcknowledged() {
        final var reason = publishRejection("account.42.billing.created", "account.*.orders.>");

        assertThat(reason).hasCauseInstanceOf(IllegalArgumentException.class);
        assertThat(reason.getCause()).hasMessageContaining("account.42.billing.created")
                .hasMessageContaining("account.*.orders.>");
    }

    @Test
    void publishSubjectIsMatchedTokenAware() {
        final var reason = publishRejection("accounting.x", "account.>");

        assertThat(reason).hasCauseInstanceOf(IllegalArgumentException.class);
    }

    /** Publishes to the subject through a channel configured with the pattern and returns the nack reason. */
    private Throwable publishRejection(String subject, String pattern) {
        final var nack = new AtomicReference<Throwable>();
        final var headers = PublishHeaders.of();
        headers.setSubject(subject);
        final var message = Message.of("created", Metadata.of(headers),
                metadata -> CompletableFuture.completedFuture(null),
                (reason, metadata) -> {
                    nack.set(reason);
                    return CompletableFuture.completedFuture(null);
                });

        client.publish(message, "client-orders", pattern).await().atMost(TIMEOUT);

        assertThat(nack.get()).isNotNull();
        return nack.get();
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

        var entry = keyValue.put("data", toBytes(data)).await().atMost(TIMEOUT);
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
    void keyValueCreate() {
        final var keyValue = client.keyValue("client");

        var entry = keyValue.create("create", "value".getBytes(StandardCharsets.UTF_8)).await().atMost(TIMEOUT);
        assertThat(entry.revision()).isGreaterThan(0L);
        assertThat(entry.value().map(String::new)).hasValue("value");

        assertThatThrownBy(() -> keyValue.create("create", "other".getBytes(StandardCharsets.UTF_8)).await()
                .atMost(TIMEOUT))
                .isInstanceOf(KeyValueException.class);

        // a deleted key can be created again
        keyValue.delete("create").await().atMost(TIMEOUT);
        entry = keyValue.create("create", "again".getBytes(StandardCharsets.UTF_8)).await().atMost(TIMEOUT);
        assertThat(entry.value().map(String::new)).hasValue("again");
        keyValue.delete("create").await().atMost(TIMEOUT);
    }

    @Test
    void keyValueCreateWithTtl() {
        final var keyValue = client.keyValue("client-ttl");

        var entry = keyValue.create("create-ttl", "value".getBytes(StandardCharsets.UTF_8), Duration.ofSeconds(1)).await()
                .atMost(TIMEOUT);
        assertThat(entry.revision()).isGreaterThan(0L);
        assertThat(entry.value().map(String::new)).hasValue("value");

        assertThatThrownBy(() -> keyValue.create("create-ttl", "other".getBytes(StandardCharsets.UTF_8),
                Duration.ofSeconds(1)).await().atMost(TIMEOUT))
                .isInstanceOf(KeyValueException.class);

        Awaitility.await().atMost(TIMEOUT)
                .until(() -> keyValue.get("create-ttl").await().atMost(TIMEOUT) == null);
        // once expired, the key can be created again
        entry = keyValue.create("create-ttl", "again".getBytes(StandardCharsets.UTF_8), Duration.ofSeconds(1)).await()
                .atMost(TIMEOUT);
        assertThat(entry.value().map(String::new)).hasValue("again");
    }

    @Test
    void keyValuePurgeWithTtl() throws Exception {
        final var keyValue = client.keyValue("client-ttl");

        keyValue.put("purge-ttl", "value".getBytes(StandardCharsets.UTF_8)).await().atMost(TIMEOUT);
        keyValue.purge("purge-ttl", Duration.ofSeconds(1)).await().atMost(TIMEOUT);
        assertThat(keyValue.get("purge-ttl").await().atMost(TIMEOUT)).isNull();

        // the purge marker itself expires, leaving no history for the key
        final var nativeKeyValue = client.nativeConnection().keyValue("client-ttl");
        Awaitility.await().atMost(TIMEOUT).until(() -> nativeKeyValue.history("purge-ttl").isEmpty());
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

    private static Message<String> messageTo(String subject) {
        final var headers = PublishHeaders.of();
        headers.setSubject(subject);
        return Message.of("created", Metadata.of(headers));
    }

    private <T> byte[] toBytes(T payload) {
        final var objectMapper = new ObjectMapper();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            objectMapper.writeValue(outputStream, payload);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
