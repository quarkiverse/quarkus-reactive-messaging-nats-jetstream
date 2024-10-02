package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.nats.client.api.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;

@Mapper
@RegisterForReflection(targets = ConsumerMapperImpl.class)
public interface ConsumerMapper {

    @Mapping(source = "numPending", target = "pending")
    @Mapping(source = "numWaiting", target = "waiting")
    @Mapping(source = "numAckPending", target = "acknowledgePending")
    @Mapping(source = "clusterInfo", target = "cluster")
    Consumer of(ConsumerInfo consumerInfo);

}
