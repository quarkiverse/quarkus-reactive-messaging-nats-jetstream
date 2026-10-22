package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;

@Mapper
public interface DiscardPolicyMapper {

    io.nats.client.api.DiscardPolicy map(DiscardPolicy policy);

    DiscardPolicy map(io.nats.client.api.DiscardPolicy policy);
}
