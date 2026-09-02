package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = { OptionalMapper.class, PlacementMapper.class, MirrorMapper.class, ExternalMapper.class, SourceMapper.class,
        RepublishMapper.class })
interface KeyValueConfigurationMapper {

    @Mapping(target = "compression", source = "compressed")
    @Mapping(target = "metadata", expression = "java(java.util.Optional.ofNullable(configuration.getMetadata()).orElseGet(java.util.Map::of))")
    KeyValueConfigurationImpl map(io.nats.client.api.KeyValueConfiguration configuration);

    @Mapping(target = "name", expression = "java(configuration.bucketName())")
    @Mapping(target = "description", expression = "java(configuration.description().orElse(null))")
    @Mapping(target = "maxHistoryPerKey", expression = "java(configuration.maxHistoryPerKey())")
    @Mapping(target = "maxBucketSize", expression = "java(configuration.maxBucketSize().orElse(-1L))")
    @Mapping(target = "maxValueSize", ignore = true)
    @Mapping(target = "maximumValueSize", ignore = true)
    @Mapping(target = "ttl", expression = "java(configuration.ttl().orElse(null))")
    @Mapping(target = "storageType", expression = "java(map(configuration.storageType()))")
    @Mapping(target = "replicas", expression = "java(configuration.replicas())")
    @Mapping(target = "metadata", expression = "java(configuration.metadata())")
    @Mapping(target = "republish", expression = "java(configuration.republish().map(republishMapper::map).orElse(null))")
    @Mapping(target = "mirror", expression = "java(configuration.mirror().map(mirrorMapper::map).orElse(null))")
    @Mapping(target = "sources", ignore = true)
    @Mapping(target = "limitMarker", ignore = true)
    @Mapping(target = "compression", expression = "java(configuration.compression())")
    @Mapping(target = "placement", expression = "java(configuration.placement().map(placementMapper::map).orElse(null))")
    io.nats.client.api.KeyValueConfiguration map(KeyValueConfiguration configuration);

    default io.nats.client.api.StorageType map(StorageType storageType) {
        final var mapper = Mappers.getMapper(StorageTypeMapper.class);
        return mapper.map(storageType);
    }
}
