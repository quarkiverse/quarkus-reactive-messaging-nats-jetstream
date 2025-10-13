package io.quarkiverse.reactive.messaging.nats.jetstream.test.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {
    static final Duration TIMEOUT = Duration.ofSeconds(10);

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addClasses(TestSpanExporter.class, Data.class))
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
        assertThat(result.purgeCount()).isEqualTo(0L);
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

        var sequence  = metadata.get().sequence();
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
}
