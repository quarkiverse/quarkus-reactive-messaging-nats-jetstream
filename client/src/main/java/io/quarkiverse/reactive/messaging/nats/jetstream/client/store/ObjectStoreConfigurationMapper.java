package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = { OptionalMapper.class, PlacementMapper.class })
public interface ObjectStoreConfigurationMapper {

    @Mapping(target = "name", source = "bucketName")
    @Mapping(target = "compression", source = "compressed")
    ObjectStoreConfiguration map(io.nats.client.api.ObjectStoreConfiguration configuration);

    default io.nats.client.api.ObjectStoreConfiguration map(ObjectStoreConfiguration configuration) {
        final var storageTypeMapper = Mappers.getMapper(StorageTypeMapper.class);
        final var placementMapper = Mappers.getMapper(PlacementMapper.class);

        var builder = io.nats.client.api.ObjectStoreConfiguration.builder()
                .name(configuration.name())
                .storageType(storageTypeMapper.map(configuration.storageType()));
        if (configuration.description().isPresent()) {
            builder = builder.description(configuration.description().get());
        }
        if (configuration.maxBucketSize().isPresent()) {
            builder = builder.maxBucketSize(configuration.maxBucketSize().get());
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
