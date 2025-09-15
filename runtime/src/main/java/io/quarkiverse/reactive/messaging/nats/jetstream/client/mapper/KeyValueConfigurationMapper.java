package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import io.nats.client.api.KeyValueConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueStoreConfiguration;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeyValueConfigurationMapper {

    public KeyValueConfiguration of(final String name, final KeyValueStoreConfiguration keyValueStoreConfiguration) {
        var builder = KeyValueConfiguration.builder();
        builder = builder.name(name);
        builder = keyValueStoreConfiguration.description().map(builder::description).orElse(builder);
        builder = builder.storageType(keyValueStoreConfiguration.storageType());
        builder = keyValueStoreConfiguration.maxBucketSize().map(builder::maxBucketSize).orElse(builder);
        builder = keyValueStoreConfiguration.maxHistoryPerKey().map(builder::maxHistoryPerKey).orElse(builder);
        builder = keyValueStoreConfiguration.maxValueSize().map(builder::maximumValueSize).orElse(builder);
        builder = keyValueStoreConfiguration.ttl().map(builder::ttl).orElse(builder);
        builder = keyValueStoreConfiguration.replicas().map(builder::replicas).orElse(builder);
        builder = builder.compression(keyValueStoreConfiguration.compressed().orElse(false));
        return builder.build();
    }
}
