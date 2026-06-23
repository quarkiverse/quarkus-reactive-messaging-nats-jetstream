package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import org.mapstruct.Mapper;

@Mapper
public interface ReplayPolicyMapper {
    io.nats.client.api.ReplayPolicy map(ReplayPolicy source);
}
