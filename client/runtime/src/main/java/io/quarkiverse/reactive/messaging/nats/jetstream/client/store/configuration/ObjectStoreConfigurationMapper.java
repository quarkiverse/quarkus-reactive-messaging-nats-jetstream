package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = { OptionalMapper.class, PlacementMapper.class })
interface ObjectStoreConfigurationMapper {

    @Mapping(target = "compression", source = "compressed")
    @Mapping(target = "metadata", expression = "java(Optional.ofNullable(configuration.getMetadata()).orElseGet(java.util.Map::of))")
    ObjectStoreConfigurationImpl map(io.nats.client.api.ObjectStoreConfiguration configuration);

    @Mapping(target = "name", expression = "java(configuration.bucketName())")
    @Mapping(target = "description", expression = "java(configuration.description().orElse(null))")
    @Mapping(target = "maxBucketSize", expression = "java(configuration.maxBucketSize().orElse(-1L))")
    @Mapping(target = "ttl", expression = "java(configuration.ttl().orElse(null))")
    @Mapping(target = "storageType", expression = "java(map(configuration.storageType()))")
    @Mapping(target = "replicas", expression = "java(configuration.replicas())")
    @Mapping(target = "metadata", expression = "java(configuration.metadata())")
    @Mapping(target = "compression", expression = "java(configuration.compression())")
    @Mapping(target = "placement", expression = "java(configuration.placement().map(placementMapper::map).orElse(null))")
    io.nats.client.api.ObjectStoreConfiguration map(ObjectStoreConfiguration configuration);

    default io.nats.client.api.StorageType map(StorageType storageType) {
        final var mapper = Mappers.getMapper(StorageTypeMapper.class);
        return mapper.map(storageType);
    }

}
