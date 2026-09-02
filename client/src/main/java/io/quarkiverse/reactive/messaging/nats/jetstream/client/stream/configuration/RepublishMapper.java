package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface RepublishMapper {

    RepublishImpl map(io.nats.client.api.Republish source);

    @Mapping(target = "source", expression = "java(republish.source())")
    @Mapping(target = "destination", expression = "java(republish.destination())")
    @Mapping(target = "headersOnly", expression = "java(republish.headersOnly())")
    io.nats.client.api.Republish map(Republish republish);

    @Mapping(target = "source", expression = "java(republish.source())")
    @Mapping(target = "destination", expression = "java(republish.destination())")
    @Mapping(target = "headersOnly", expression = "java(republish.headersOnly())")
    RepublishImpl to(Republish republish);
}
