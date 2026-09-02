package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface SubjectTransformMapper {

    SubjectTransformImpl map(io.nats.client.api.SubjectTransform source);

    @Mapping(target = "source", expression = "java(value.source())")
    @Mapping(target = "destination", expression = "java(value.destination())")
    io.nats.client.api.SubjectTransform map(SubjectTransform value);

    @Mapping(target = "source", expression = "java(value.source())")
    @Mapping(target = "destination", expression = "java(value.destination())")
    SubjectTransformImpl to(SubjectTransform value);
}
