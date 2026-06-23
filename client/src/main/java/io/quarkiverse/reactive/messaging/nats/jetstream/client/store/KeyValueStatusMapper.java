package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamInfoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {OptionalMapper.class, StreamInfoMapper.class, KeyValueConfigurationMapper.class})
public interface KeyValueStatusMapper {

    @Mapping(target = "streamInfo", source = "backingStreamInfo")
    KeyValueStatus map(io.nats.client.api.KeyValueStatus status);
}
