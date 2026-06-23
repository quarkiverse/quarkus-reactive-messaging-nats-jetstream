package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import org.mapstruct.Mapper;

@Mapper
interface DeliverPolicyMapper {
    io.nats.client.api.DeliverPolicy map(DeliverPolicy source);
}
