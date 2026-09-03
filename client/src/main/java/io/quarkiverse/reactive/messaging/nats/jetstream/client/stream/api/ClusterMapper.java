package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(uses = { OptionalMapper.class,
        ReplicaMapper.class }, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface ClusterMapper {

    ClusterImpl map(io.nats.client.api.ClusterInfo source);

}
