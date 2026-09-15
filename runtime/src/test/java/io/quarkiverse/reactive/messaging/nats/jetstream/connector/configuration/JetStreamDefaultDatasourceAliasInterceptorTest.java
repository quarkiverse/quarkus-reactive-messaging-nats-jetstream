package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JetStreamDefaultDatasourceAliasInterceptorTest {

    @Test
    void mapsRootPropertiesToTheDefaultAlias() {
        assertThat(JetStreamDefaultDatasourceAliasInterceptor.mapName("quarkus.messaging.nats.connection.servers[0]"))
                .isEqualTo("quarkus.messaging.nats.default.connection.servers[0]");
        assertThat(JetStreamDefaultDatasourceAliasInterceptor.mapName("quarkus.messaging.nats.streams.test.subjects"))
                .isEqualTo("quarkus.messaging.nats.default.streams.test.subjects");
    }

    @Test
    void mapsTheDefaultAliasBackToRootProperties() {
        assertThat(JetStreamDefaultDatasourceAliasInterceptor
                .mapName("quarkus.messaging.nats.default.connection.servers[0]"))
                .isEqualTo("quarkus.messaging.nats.connection.servers[0]");
        assertThat(JetStreamDefaultDatasourceAliasInterceptor.mapName("quarkus.messaging.nats.default.streams.test.subjects"))
                .isEqualTo("quarkus.messaging.nats.streams.test.subjects");
    }

    @Test
    void isItsOwnInverse() {
        final var names = new String[] {
                "quarkus.messaging.nats.connection.servers[0]",
                "quarkus.messaging.nats.default.connection.servers[0]",
                "quarkus.messaging.nats.streams.test.subjects",
        };
        for (String name : names) {
            assertThat(JetStreamDefaultDatasourceAliasInterceptor
                    .mapName(JetStreamDefaultDatasourceAliasInterceptor.mapName(name)))
                    .isEqualTo(name);
        }
    }

    @Test
    void leavesNamedDatasourcesUntouched() {
        final var name = "quarkus.messaging.nats.data-sources.foo.connection.servers[0]";
        assertThat(JetStreamDefaultDatasourceAliasInterceptor.mapName(name)).isEqualTo(name);
        assertThat(JetStreamDefaultDatasourceAliasInterceptor.mapName("quarkus.messaging.nats.data-sources"))
                .isEqualTo("quarkus.messaging.nats.data-sources");
    }

    @Test
    void leavesTheBareDefaultSegmentAndUnrelatedPropertiesUntouched() {
        assertThat(JetStreamDefaultDatasourceAliasInterceptor.mapName("quarkus.messaging.nats.default"))
                .isEqualTo("quarkus.messaging.nats.default");
        assertThat(JetStreamDefaultDatasourceAliasInterceptor.mapName("quarkus.unrelated.prop"))
                .isEqualTo("quarkus.unrelated.prop");
    }
}
