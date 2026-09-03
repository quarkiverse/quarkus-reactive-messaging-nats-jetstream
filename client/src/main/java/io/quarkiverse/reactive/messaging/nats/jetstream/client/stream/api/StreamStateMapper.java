package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(uses = { OptionalMapper.class, LostStreamDataMapper.class,
        SubjectMapper.class }, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface StreamStateMapper {

    @Mapping(target = "messageCount", source = "msgCount")
    StreamStateImpl map(io.nats.client.api.StreamState source);

}
