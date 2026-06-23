package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface KeyValueEntryMapper {

    @Mapping(target = "dataLength", source = "dataLen")
    KeyValueEntry map(io.nats.client.api.KeyValueEntry keyValueEntry);
}
