package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;

@Mapper(uses = { OptionalMapper.class })
public interface StreamAlternateMapper {

    StreamAlternateImpl map(io.nats.client.api.StreamAlternate streamAlternate);

}
