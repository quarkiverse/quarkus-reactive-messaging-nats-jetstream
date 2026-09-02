package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;

@Mapper
interface ErrorMapper {

    ErrorImpl map(io.nats.client.api.Error source);
}
