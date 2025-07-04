package io.quarkiverse.reactive.messaging.nats.jetstream.test.configuration;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;


public class ConsumerConfigurationConverterTest {

    @RegisterExtension
    final static QuarkusUnitTest quarkusTest = new  QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource("application-configuration.properties", "application.properties"));

    @Inject
    JetStreamConfiguration jetStreamConfiguration;

    @Test
    void parseConsumers() {
        assertThat(jetStreamConfiguration).isNotNull();
    }
}
