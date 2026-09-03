package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(uses = { OptionalMapper.class, ExternalMapper.class, ErrorMapper.class,
        SubjectTransformMapper.class }, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface MirrorMapper {

    MirrorImpl map(io.nats.client.api.MirrorInfo source);

}
