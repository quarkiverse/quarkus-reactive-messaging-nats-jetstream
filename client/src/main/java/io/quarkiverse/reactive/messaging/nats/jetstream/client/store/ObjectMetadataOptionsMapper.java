package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;

@Mapper(uses = { OptionalMapper.class, ObjectLinkMapper.class })
interface ObjectMetadataOptionsMapper {

    ObjectMetadataOptions map(io.nats.client.api.ObjectMetaOptions options);

}
