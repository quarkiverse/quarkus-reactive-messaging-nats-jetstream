package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreConfigurationImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

@ApplicationScoped
public class KeyValueStoreConfigurationMapperImpl implements KeyValueStoreConfigurationMapper {

    @Override
    public List<KeyValueStoreConfiguration> map(
            ConnectorConfiguration configuration) {
        return configuration.keyValueStores().entrySet().stream().map(entry -> map(entry.getKey(), entry.getValue())).toList();
    }

    private io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreConfiguration map(String name,
            io.quarkiverse.reactive.messaging.nats.jetstream.configuration.KeyValueStoreConfiguration configuration) {
        return KeyValueStoreConfigurationImpl.builder()
                .name(name)
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
