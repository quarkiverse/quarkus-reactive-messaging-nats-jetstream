package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.nats.client.api.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;

@Mapper(componentModel = "cdi")
public interface ConsumerMapper {

    @Mapping(source = "numPending", target = "pending")
    @Mapping(source = "numWaiting", target = "waiting")
    @Mapping(source = "numAckPending", target = "acknowledgePending")
    @Mapping(source = "clusterInfo", target = "cluster")
    @Mapping(source = "consumerConfiguration", target = "configuration")
    @Mapping(source = "streamName", target = "stream")
    @Mapping(source = "creationTime", target = "created")
    Consumer of(ConsumerInfo consumerInfo);

}
