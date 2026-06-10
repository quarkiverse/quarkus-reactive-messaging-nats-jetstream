package io.quarkiverse.reactive.messaging.nats.configuration.mapper;

import java.util.List;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.configuration.KeyValueStoreConfiguration;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.client.store.KeyValueStoreConfigurationImpl;
import io.quarkiverse.reactive.messaging.nats.configuration.ConnectorConfiguration;

@ApplicationScoped
public class KeyValueStoreConfigurationMapperImpl implements KeyValueStoreConfigurationMapper {

    @Override
    public List<io.quarkiverse.reactive.messaging.nats.client.store.KeyValueStoreConfiguration> map(ConnectorConfiguration configuration) {
        return Optional.ofNullable(configuration.keyValueStores())
                .map(keyValueStores -> keyValueStores.entrySet().stream().map(entry -> map(entry.getKey(), entry.getValue()))
                        .toList())
                .orElseGet(List::of);

    }

    private io.quarkiverse.reactive.messaging.nats.client.store.KeyValueStoreConfiguration map(String name,
                                                                                               KeyValueStoreConfiguration configuration) {
        return KeyValueStoreConfigurationImpl.builder()
                .name(configuration.bucketName().orElse(name))
                .description(configuration.description())
                .ttl(configuration.ttl())
                .maxHistoryPerKey(configuration.maxHistoryPerKey())
                .maxValueSize(configuration.maxValueSize())
                .compressed(configuration.compressed())
                .maxBucketSize(configuration.maxBucketSize())
                .replicas(configuration.replicas())
                .storageType(configuration.storageType())
                .build();
    }
}
