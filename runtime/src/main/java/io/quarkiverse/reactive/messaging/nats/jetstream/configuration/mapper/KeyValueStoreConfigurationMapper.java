package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreConfigurationImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.KeyValueStoreConfiguration;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface KeyValueStoreConfigurationMapper {

    default List<io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreConfiguration> map(ConnectorConfiguration configuration) {
        return configuration.keyValueStores().entrySet().stream().map(entry -> map(entry.getKey(), entry.getValue())).toList();
    }

    private io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreConfiguration map(String name, KeyValueStoreConfiguration configuration) {
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
