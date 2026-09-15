package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import static io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration.DEFAULT_DATASOURCE;

import io.smallrye.config.FallbackConfigSourceInterceptor;

/**
 * Lets the default (unnamed) datasource be configured either at the flat root
 * ({@code quarkus.messaging.nats.connection.*}, {@code quarkus.messaging.nats.streams.*}, etc. - as
 * {@link ConnectorConfiguration}, which extends {@link DataSourceConfiguration}, already binds it) or,
 * symmetrically with named datasources, under {@code quarkus.messaging.nats.default.*}
 * (e.g. {@code quarkus.messaging.nats.default.connection.*}). Both spellings are accepted as aliases for exactly
 * the same underlying properties; whichever one is actually set wins (if both are set, the one from the
 * higher-priority config source wins). {@code quarkus.messaging.nats.data-sources.*} (named datasources) is left
 * untouched - it is a distinct, already-nested configuration and isn't part of this aliasing.
 * <p>
 * Registered via {@code META-INF/services/io.smallrye.config.ConfigSourceInterceptor} so it participates in both
 * plain property lookups ({@code getValue}) and the property-name enumeration
 * ({@code AbstractMappingConfigSourceInterceptor#iterateNames}) that SmallRye Config's {@code @ConfigMapping}
 * binder uses to discover list/map entries (e.g. {@code connection.servers[0]}).
 */
public class JetStreamDefaultDatasourceAliasInterceptor extends FallbackConfigSourceInterceptor {
    private static final String ROOT_PREFIX = "quarkus.messaging.nats.";
    private static final String DEFAULT_PREFIX = ROOT_PREFIX + DEFAULT_DATASOURCE + ".";

    public JetStreamDefaultDatasourceAliasInterceptor() {
        super(JetStreamDefaultDatasourceAliasInterceptor::mapName);
    }

    /**
     * Maps a {@code quarkus.messaging.nats.default.<rest>} name to its root-space equivalent
     * {@code quarkus.messaging.nats.<rest>}, and vice versa. Names under {@code quarkus.messaging.nats.data-sources}
     * (and the bare {@code default}/{@code data-sources} segments themselves) are returned unchanged, since they
     * aren't part of this aliasing.
     */
    static String mapName(String name) {
        if (name.startsWith(DEFAULT_PREFIX)) {
            return ROOT_PREFIX + name.substring(DEFAULT_PREFIX.length());
        }
        if (!name.startsWith(ROOT_PREFIX)) {
            return name;
        }
        final var remainder = name.substring(ROOT_PREFIX.length());
        if (remainder.equals(DEFAULT_DATASOURCE) || remainder.startsWith(DEFAULT_DATASOURCE + ".")
                || remainder.equals("data-sources") || remainder.startsWith("data-sources.")) {
            return name;
        }
        return DEFAULT_PREFIX + remainder;
    }
}
