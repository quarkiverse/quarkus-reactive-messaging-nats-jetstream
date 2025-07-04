package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.Map;

import io.nats.client.Connection;
import io.nats.client.api.KeyValueConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueStoreConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class DefaultKeyValueStoreManagement implements KeyValueStoreManagement {
    private final Connection connection;
    private final Vertx vertx;

    @Override
    public Uni<Void> addKeyValueStores(Map<String, ? extends KeyValueStoreConfiguration> keyValueConfigurations) {
        return context().executeBlocking(Multi.createFrom().items(keyValueConfigurations.entrySet().stream())
                .onItem().transformToUniAndMerge(entry -> addOrUpdateKeyValueStore(entry.getKey(), entry.getValue()))
                .collect().last());
    }

    private Uni<Void> addOrUpdateKeyValueStore(final String name, final KeyValueStoreConfiguration keyValueStoreConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var kvm = connection.keyValueManagement();
                if (kvm.getBucketNames().contains(name)) {
                    kvm.update(createConfiguration(name, keyValueStoreConfiguration));
                } else {
                    kvm.create(createConfiguration(name, keyValueStoreConfiguration));
                }
                return null;
            } catch (Exception failure) {
                throw new SetupException(String.format("Unable to manage Key Value Store: %s", failure.getMessage()),
                        failure);
            }
        }));
    }

    private Context context() {
        return vertx.getOrCreateContext();
    }

    private KeyValueConfiguration createConfiguration(final String name,
            final KeyValueStoreConfiguration keyValueStoreConfiguration) {
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
