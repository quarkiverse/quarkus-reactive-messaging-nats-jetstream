package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = OptionalMapper.class)
interface ConsumerLimitsMapper {

    ConsumerLimitsImpl map(io.nats.client.api.ConsumerLimits source);

    @Mapping(target = "inactiveThreshold", expression = "java(value.inactiveThreshold().orElse(null))")
    @Mapping(target = "maxAckPending", expression = "java(value.maxAckPending())")
    io.nats.client.api.ConsumerLimits map(ConsumerLimits value);

    @Mapping(target = "inactiveThreshold", expression = "java(value.inactiveThreshold())")
    @Mapping(target = "maxAckPending", expression = "java(value.maxAckPending())")
    ConsumerLimitsImpl to(ConsumerLimits value);
}
