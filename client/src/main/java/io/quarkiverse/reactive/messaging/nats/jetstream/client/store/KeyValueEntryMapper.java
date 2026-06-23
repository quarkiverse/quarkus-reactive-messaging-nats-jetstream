package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;

@Mapper
public interface KeyValueEntryMapper {

    KeyValueEntry map(io.nats.client.api.KeyValueEntry keyValueEntry);
}
