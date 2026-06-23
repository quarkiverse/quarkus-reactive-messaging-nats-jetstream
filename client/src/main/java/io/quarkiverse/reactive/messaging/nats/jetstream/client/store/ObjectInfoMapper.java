package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = OptionalMapper.class)
public interface ObjectInfoMapper {

    @Mapping(target = "nuId", source = "nuid")
    @Mapping(target = "metadata", source = "objectMeta")
    ObjectInfo map(io.nats.client.api.ObjectInfo objectInfo);

    io.nats.client.api.ObjectInfo map(ObjectInfo objectInfo);
}
