package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = { OptionalMapper.class })
interface PullOptionsMapper {

    @Mapping(target = "maxWaiting", source = "maxPullWaiting")
    @Mapping(target = "maxExpires", source = "maxExpires")
    @Mapping(target = "maxBatch", source = "maxBatch")
    @Mapping(target = "maxBytes", source = "maxBytes")
    PullOptionsImpl map(io.nats.client.api.ConsumerConfiguration source);

}
