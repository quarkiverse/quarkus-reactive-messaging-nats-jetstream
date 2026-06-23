package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;

@Mapper
public interface ObjectStoreStatusMapper {

    ObjectStoreStatus map(io.nats.client.api.ObjectStoreStatus status);
}
