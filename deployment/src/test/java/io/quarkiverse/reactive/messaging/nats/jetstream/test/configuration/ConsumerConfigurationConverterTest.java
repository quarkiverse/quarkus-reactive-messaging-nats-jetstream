package io.quarkiverse.reactive.messaging.nats.jetstream.test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;
import io.quarkus.test.QuarkusUnitTest;

public class ConsumerConfigurationConverterTest {

    @RegisterExtension
    final static QuarkusUnitTest quarkusTest = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource("application-configuration.properties", "application.properties"));

    @Inject
    JetStreamConfiguration jetStreamConfiguration;

    @Test
    void parseConsumers() {
        assertThat(jetStreamConfiguration).isNotNull();
        assertThat(jetStreamConfiguration.streams()).hasFieldOrProperty("test");



    }
}
