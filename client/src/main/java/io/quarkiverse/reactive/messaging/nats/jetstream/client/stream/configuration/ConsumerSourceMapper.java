package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface ConsumerSourceMapper {

    ConsumerSourceImpl map(io.nats.client.api.ConsumerSource source);

    @Mapping(target = "name", expression = "java(value.name())")
    @Mapping(target = "deliverSubject", expression = "java(value.deliverSubject())")
    ConsumerSourceImpl to(ConsumerSource value);

}
