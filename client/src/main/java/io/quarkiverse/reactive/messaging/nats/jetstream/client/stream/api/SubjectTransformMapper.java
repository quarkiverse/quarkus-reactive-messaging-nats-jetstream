package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;

@Mapper
interface SubjectTransformMapper {

    SubjectTransformImpl map(io.nats.client.api.SubjectTransform source);
}
