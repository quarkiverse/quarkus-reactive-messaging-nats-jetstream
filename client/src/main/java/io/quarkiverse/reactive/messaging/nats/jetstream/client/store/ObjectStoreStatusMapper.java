package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamInfoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {OptionalMapper.class, PlacementMapper.class, StreamInfoMapper.class, ObjectStoreConfigurationMapper.class})
public interface ObjectStoreStatusMapper {

    @Mapping(target = "streamInfo", source = "backingStreamInfo")
    @Mapping(target = "sealed", expression = "java(status.getConfiguration().isSealed())")
    ObjectStoreStatus map(io.nats.client.api.ObjectStoreStatus status);


}
