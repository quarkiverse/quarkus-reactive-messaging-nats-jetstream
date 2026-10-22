package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;

@Mapper
interface SubjectMapper {

    SubjectImpl map(io.nats.client.api.Subject subject);

}
