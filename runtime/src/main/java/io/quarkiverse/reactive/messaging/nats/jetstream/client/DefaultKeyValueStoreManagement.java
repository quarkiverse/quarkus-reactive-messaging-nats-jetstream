package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.List;

import io.nats.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueConfigurationFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueSetupConfiguration;
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
    public Uni<Void> addKeyValueStores(List<KeyValueSetupConfiguration> keyValueConfigurations) {
        return context().executeBlocking(Multi.createFrom().items(keyValueConfigurations.stream())
                .onItem().transformToUniAndMerge(this::addOrUpdateKeyValueStore)
                .collect().last());
    }

    private Uni<Void> addOrUpdateKeyValueStore(final KeyValueSetupConfiguration keyValueSetupConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var kvm = connection.keyValueManagement();
                final var factory = new KeyValueConfigurationFactory();
                if (kvm.getBucketNames().contains(keyValueSetupConfiguration.bucketName())) {
                    kvm.update(factory.create(keyValueSetupConfiguration));
                } else {
                    kvm.create(factory.create(keyValueSetupConfiguration));
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
}
