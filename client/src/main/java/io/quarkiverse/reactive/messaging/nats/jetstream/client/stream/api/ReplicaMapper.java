package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;

@Mapper
interface ReplicaMapper {

    ReplicaImpl map(io.nats.client.api.Replica source);

}
