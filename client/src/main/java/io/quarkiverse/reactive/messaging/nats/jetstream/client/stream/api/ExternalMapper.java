package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;

@Mapper(uses = OptionalMapper.class)
interface ExternalMapper {

    ExternalImpl map(io.nats.client.api.External source);

}
