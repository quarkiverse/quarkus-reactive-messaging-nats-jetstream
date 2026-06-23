package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {OptionalMapper.class, PlacementMapper.class})
public interface ObjectStoreConfigurationMapper {

    @Mapping(target = "name", source = "bucketName")
    ObjectStoreConfiguration map(io.nats.client.api.ObjectStoreConfiguration configuration);

}
