package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {OptionalMapper.class, PlacementMapper.class})
public interface KeyValueConfigurationMapper {

    @Mapping(target = "name", source = "bucketName")
    @Mapping(target = "compression", source = "compressed")
    KeyValueConfiguration map(io.nats.client.api.KeyValueConfiguration configuration);

    default io.nats.client.api.KeyValueConfiguration map(KeyValueConfiguration configuration) {
        final var storageTypeMapper = Mappers.getMapper(StorageTypeMapper.class);
        final var placementMapper = Mappers.getMapper(PlacementMapper.class);

        var builder = io.nats.client.api.KeyValueConfiguration.builder()
                .name(configuration.name())
                .storageType(storageTypeMapper.map(configuration.storageType()));
        if (configuration.description().isPresent()) {
            builder = builder.description(configuration.description().get());
        }
        if (configuration.maxBucketSize().isPresent()) {
            builder = builder.maxBucketSize(configuration.maxBucketSize().get());
        }
        if (configuration.maxHistoryPerKey().isPresent()) {
            builder = builder.maxHistoryPerKey(configuration.maxHistoryPerKey().get());
        }
        if (configuration.maxValueSize().isPresent()) {
            builder = builder.maximumValueSize(configuration.maxValueSize().get());
        }
        if (configuration.ttl().isPresent()) {
            builder = builder.ttl(configuration.ttl().get());
        }
        if (configuration.replicas().isPresent()) {
            builder = builder.replicas(configuration.replicas().get());
        }
        if (configuration.compression().isPresent()) {
            builder = builder.compression(configuration.compression().get());
        }
        if (configuration.placement().isPresent()) {
            builder = builder.placement(placementMapper.map(configuration.placement().get()));
        }
        return builder.build();
    }
}
