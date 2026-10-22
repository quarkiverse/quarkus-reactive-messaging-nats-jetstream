package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;

@Mapper(uses = { OptionalMapper.class, ExternalMapper.class, SubjectTransformMapper.class, ErrorMapper.class })
interface SourceMapper {

    SourceImpl map(io.nats.client.api.SourceInfo sourceInfo);

}
