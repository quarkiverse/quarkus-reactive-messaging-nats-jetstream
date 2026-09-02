package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.ConsumerSource;

@Mapper
interface ConsumerSourceMapper {

    ConsumerSourceImpl map(io.nats.client.api.ConsumerSource source);

    @Mapping(target = "name", expression = "java(value.name())")
    @Mapping(target = "deliverSubject", expression = "java(value.deliverSubject())")
    ConsumerSourceImpl to(ConsumerSource value);

}
