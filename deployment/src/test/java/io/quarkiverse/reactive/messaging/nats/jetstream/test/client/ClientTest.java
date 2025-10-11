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

}
