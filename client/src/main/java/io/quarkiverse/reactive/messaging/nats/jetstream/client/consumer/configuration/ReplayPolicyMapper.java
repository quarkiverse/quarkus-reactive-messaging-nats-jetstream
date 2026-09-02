package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration;

import org.mapstruct.Mapper;

@Mapper
interface ReplayPolicyMapper {
    io.nats.client.api.ReplayPolicy map(ReplayPolicy source);
}
