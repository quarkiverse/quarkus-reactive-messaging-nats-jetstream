package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.External;

@Mapper(uses = OptionalMapper.class)
interface ExternalMapper {

    ExternalImpl map(io.nats.client.api.External source);

    @Mapping(target = "api", expression = "java(source.api().orElse(null))")
    @Mapping(target = "deliver", expression = "java(source.deliver().orElse(null))")
    io.nats.client.api.External map(External source);

    @Mapping(target = "api", expression = "java(source.api())")
    @Mapping(target = "deliver", expression = "java(source.deliver())")
    ExternalImpl to(External source);

}
