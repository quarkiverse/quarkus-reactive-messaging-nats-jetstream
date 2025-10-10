package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import io.nats.client.api.KeyValueConfiguration;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeyValueConfigurationMapperImpl implements KeyValueConfigurationMapper {

    @Override
    public KeyValueConfiguration map(final KeyValueStoreConfiguration keyValueStoreConfiguration) {
        var builder = KeyValueConfiguration.builder();
        builder = builder.name(keyValueStoreConfiguration.name());
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
