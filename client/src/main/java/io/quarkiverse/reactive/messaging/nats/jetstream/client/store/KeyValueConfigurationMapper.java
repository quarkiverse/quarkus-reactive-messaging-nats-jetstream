package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = { OptionalMapper.class, PlacementMapper.class })
public interface KeyValueConfigurationMapper {

    @Mapping(target = "name", source = "bucketName")
    KeyValueConfiguration map(io.nats.client.api.KeyValueConfiguration configuration);
}
