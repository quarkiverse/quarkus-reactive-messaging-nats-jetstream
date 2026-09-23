package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration;
import io.quarkus.test.QuarkusExtensionTest;

/**
 * Streams and consumers configured at the flat root ({@code quarkus.messaging.nats.*}) are recognized configuration
 * keys - the default datasource alias must not turn them into unrecognized {@code default.*} twins.
 */
public class DefaultDatasourceRootConfigurationTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .withConfigurationResource("application-default-datasource-root.properties")
            .setLogRecordPredicate(record -> record.getMessage() != null
                    && record.getMessage().contains("Unrecognized configuration key"))
            .assertLogRecords(records -> assertThat(records).isEmpty());

    @Inject
    ConnectorConfiguration configuration;

    @Test
    public void configuresTheDefaultDatasource() {
        assertThat(configuration.streams()).containsKey("test");
        assertThat(configuration.consumers()).containsKey("data-consumer");
    }
}
