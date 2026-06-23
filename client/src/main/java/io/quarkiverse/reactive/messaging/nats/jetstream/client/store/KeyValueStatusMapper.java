package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamInfoMapper;

@Mapper(uses = { OptionalMapper.class, StreamInfoMapper.class, KeyValueConfigurationMapper.class })
public interface KeyValueStatusMapper {

    @Mapping(target = "streamInfo", source = "backingStreamInfo")
    KeyValueStatus map(io.nats.client.api.KeyValueStatus status);
}
