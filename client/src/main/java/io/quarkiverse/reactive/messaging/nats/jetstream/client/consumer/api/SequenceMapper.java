package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api;

import org.mapstruct.Mapper;

@Mapper(uses = OptionalMapper.class)
interface SequenceMapper {

    SequenceImpl map(io.nats.client.api.SequenceInfo source);
}
