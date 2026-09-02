package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration;

import org.mapstruct.Mapper;

@Mapper(uses = { OptionalMapper.class })
interface PushOptionsMapper {

    PushOptionsImpl map(io.nats.client.api.ConsumerConfiguration source);

}
