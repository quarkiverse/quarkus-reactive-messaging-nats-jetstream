package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import io.nats.client.api.KeyValueConfiguration;

public class KeyValueConfigurationFactory {

    public KeyValueConfiguration create(final KeyValueSetupConfiguration keyValueSetupConfiguration) {
        var builder = KeyValueConfiguration.builder();
        builder = builder.name(keyValueSetupConfiguration.bucketName());
        builder = keyValueSetupConfiguration.description().map(builder::description).orElse(builder);
        builder = builder.storageType(keyValueSetupConfiguration.storageType());
        builder = keyValueSetupConfiguration.maxBucketSize().map(builder::maxBucketSize).orElse(builder);
        builder = keyValueSetupConfiguration.maxHistoryPerKey().map(builder::maxHistoryPerKey).orElse(builder);
        builder = keyValueSetupConfiguration.maxValueSize().map(builder::maximumValueSize).orElse(builder);
        builder = keyValueSetupConfiguration.ttl().map(builder::ttl).orElse(builder);
        builder = keyValueSetupConfiguration.replicas().map(builder::replicas).orElse(builder);
        builder = builder.compression(keyValueSetupConfiguration.compressed());
        return builder.build();
    }
}
