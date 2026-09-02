package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = OptionalMapper.class)
public interface KeyValueEntryMapper {

    @Mapping(target = "dataLength", source = "dataLen")
    KeyValueEntryImpl map(io.nats.client.api.KeyValueEntry keyValueEntry);
}
