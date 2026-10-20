package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

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
    void enumeratesEveryNameInItsRootSpelling() {
        final var config = config(Map.of(
                "quarkus.messaging.nats.connection.servers", "nats://localhost:4222",
                "quarkus.messaging.nats.default.streams.test.name", "test",
                "quarkus.messaging.nats.data-sources.orders.streams.orders.name", "orders"));

        final var names = new HashSet<String>();
        config.getPropertyNames().forEach(names::add);

        assertThat(names)
                .contains("quarkus.messaging.nats.connection.servers",
                        "quarkus.messaging.nats.streams.test.name",
                        "quarkus.messaging.nats.data-sources.orders.streams.orders.name")
                .doesNotContain("quarkus.messaging.nats.default.connection.servers",
                        "quarkus.messaging.nats.default.streams.test.name");
    }

    @Test
    void resolvesValuesUnderBothSpellings() {
        final var config = config(Map.of(
                "quarkus.messaging.nats.connection.servers", "nats://localhost:4222",
                "quarkus.messaging.nats.default.streams.test.name", "test"));

        assertThat(config.getConfigValue("quarkus.messaging.nats.streams.test.name").getValue()).isEqualTo("test");
        assertThat(config.getConfigValue("quarkus.messaging.nats.default.streams.test.name").getValue()).isEqualTo("test");
        assertThat(config.getConfigValue("quarkus.messaging.nats.connection.servers").getValue())
                .isEqualTo("nats://localhost:4222");
        assertThat(config.getConfigValue("quarkus.messaging.nats.default.connection.servers").getValue())
                .isEqualTo("nats://localhost:4222");
    }

    @Test
    void mergesBothSpellingsOfTheSameDatasource() {
        final var config = config(Map.of(
                "quarkus.messaging.nats.streams.test.name", "root",
                "quarkus.messaging.nats.default.streams.test.name", "alias",
                "quarkus.messaging.nats.default.streams.other.name", "other"));

        final var names = new HashSet<String>();
        config.getPropertyNames().forEach(names::add);

        assertThat(names).contains("quarkus.messaging.nats.streams.test.name",
                "quarkus.messaging.nats.streams.other.name");
        assertThat(config.getConfigValue("quarkus.messaging.nats.streams.test.name").getValue()).isEqualTo("root");
        assertThat(config.getConfigValue("quarkus.messaging.nats.streams.other.name").getValue()).isEqualTo("other");
    }

    private static SmallRyeConfig config(final Map<String, String> properties) {
        return new SmallRyeConfigBuilder()
                .withSources(new PropertiesConfigSource(properties, "test", 100))
                .withInterceptors(new JetStreamDefaultDatasourceAliasInterceptor())
                .build();
    }

    @Test
    void leavesTheBareDefaultSegmentAndUnrelatedPropertiesUntouched() {
        assertThat(JetStreamDefaultDatasourceAliasInterceptor.mapName("quarkus.messaging.nats.default"))
                .isEqualTo("quarkus.messaging.nats.default");
        assertThat(JetStreamDefaultDatasourceAliasInterceptor.mapName("quarkus.unrelated.prop"))
                .isEqualTo("quarkus.unrelated.prop");
    }
}
