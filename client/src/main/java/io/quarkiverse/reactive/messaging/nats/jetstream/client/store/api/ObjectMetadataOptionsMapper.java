package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.mapstruct.Mapper;

@Mapper(uses = { OptionalMapper.class, ObjectLinkMapper.class })
interface ObjectMetadataOptionsMapper {

    ObjectMetadataOptionsImpl map(io.nats.client.api.ObjectMetaOptions options);

}
