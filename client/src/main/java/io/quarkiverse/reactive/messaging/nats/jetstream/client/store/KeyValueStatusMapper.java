package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;

@Mapper
public interface KeyValueStatusMapper {

    KeyValueStatus map(io.nats.client.api.KeyValueStatus status);
}
