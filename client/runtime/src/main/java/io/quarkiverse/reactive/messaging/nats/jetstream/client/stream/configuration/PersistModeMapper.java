package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;

@Mapper
interface PersistModeMapper {

    PersistMode map(io.nats.client.api.PersistMode source);

    io.nats.client.api.PersistMode map(PersistMode source);

}
