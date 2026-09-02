package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(uses = { OptionalMapper.class }, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface LostStreamDataMapper {

    LostStreamDataImpl map(io.nats.client.api.LostStreamData source);

}
