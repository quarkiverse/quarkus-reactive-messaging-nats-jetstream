package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = OptionalMapper.class)
interface PlacementMapper {

    PlacementImpl map(io.nats.client.api.Placement placement);

    @Mapping(target = "cluster", expression = "java(value.cluster().orElse(null))")
    @Mapping(target = "tags", expression = "java(value.tags())")
    io.nats.client.api.Placement map(Placement value);

}
