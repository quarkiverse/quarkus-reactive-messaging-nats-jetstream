package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = OptionalMapper.class)
interface PlacementMapper {

    PlacementImpl map(io.nats.client.api.Placement source);

    @Mapping(target = "cluster", expression = "java(source.cluster())")
    @Mapping(target = "tags", expression = "java(source.tags())")
    PlacementImpl to(Placement source);

    @Mapping(target = "cluster", expression = "java(value.cluster().orElse(null))")
    @Mapping(target = "tags", expression = "java(value.tags())")
    io.nats.client.api.Placement map(Placement value);
}
