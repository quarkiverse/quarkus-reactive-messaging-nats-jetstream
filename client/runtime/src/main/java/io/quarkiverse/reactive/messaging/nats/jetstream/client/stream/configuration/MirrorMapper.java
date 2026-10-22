package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = { ExternalMapper.class, ConsumerSourceMapper.class, SubjectTransformMapper.class, OptionalMapper.class })
interface MirrorMapper {

    @Mapping(target = "startSequence", source = "startSeq")
    MirrorImpl map(io.nats.client.api.Mirror source);

    io.nats.client.api.Mirror map(Mirror source);

    @Mapping(target = "sourceName", expression = "java(source.sourceName())")
    @Mapping(target = "name", expression = "java(source.name())")
    @Mapping(target = "startSequence", expression = "java(source.startSequence())")
    @Mapping(target = "startTime", expression = "java(source.startTime())")
    @Mapping(target = "filterSubject", expression = "java(source.filterSubject())")
    @Mapping(target = "external", expression = "java(source.external())")
    @Mapping(target = "subjectTransforms", expression = "java(source.subjectTransforms())")
    @Mapping(target = "consumerSource", expression = "java(source.consumerSource())")
    MirrorImpl to(Mirror source);

}
