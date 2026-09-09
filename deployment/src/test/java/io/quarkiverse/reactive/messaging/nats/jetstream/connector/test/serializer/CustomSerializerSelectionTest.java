package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.serializer;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkus.test.QuarkusExtensionTest;

class CustomSerializerSelectionTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(CustomSerializer.class))
            .overrideConfigKey("quarkus.messaging.nats.serializer", CustomSerializer.class.getName());

    @Inject
    Serializer serializer;

    @Test
    void customSerializerSelected() {
        assertThat(serializer).isInstanceOf(CustomSerializer.class);
    }
}
