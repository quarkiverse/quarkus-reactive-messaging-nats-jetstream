package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;

@Mapper
public interface ObjectMetadataMapper {

    io.nats.client.api.ObjectMeta map(ObjectMetadata metadata);
}
