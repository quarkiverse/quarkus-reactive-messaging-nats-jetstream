package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkus.runtime.annotations.RegisterForReflection;

@Mapper
@RegisterForReflection(targets = StreamStateMapperImpl.class)
public interface StreamStateMapper {

    @Mapping(source = "msgCount", target = "messages")
    @Mapping(source = "byteCount", target = "bytes")
    @Mapping(source = "consumerCount", target = "consumers")
    @Mapping(source = "subjectCount", target = "subjects")
    @Mapping(source = "deletedCount", target = "deleted")
    @Mapping(source = "subjects", target = "subjectStates")
    @Mapping(source = "subjectMap", target = "subjectMessages")
    @Mapping(source = "deleted", target = "deletedStreamSequences")
    StreamState of(io.nats.client.api.StreamState state);
}
