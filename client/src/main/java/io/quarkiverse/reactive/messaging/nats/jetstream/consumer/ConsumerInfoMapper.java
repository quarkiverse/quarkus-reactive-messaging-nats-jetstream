package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import org.mapstruct.Mapper;

@Mapper
interface ConsumerInfoMapper {

    ConsumerInfo to(io.nats.client.api.ConsumerInfo source);

}
