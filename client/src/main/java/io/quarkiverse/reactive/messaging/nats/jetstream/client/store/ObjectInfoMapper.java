package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;

@Mapper
public interface ObjectInfoMapper {

    ObjectInfo map(io.nats.client.api.ObjectInfo objectInfo);

    io.nats.client.api.ObjectInfo map(ObjectInfo objectInfo);
}
