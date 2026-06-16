package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface ConsumerInfoMapper {

    @Mapping(target = "stream", source = "streamName")
    @Mapping(target = "configuration", source = "consumerConfiguration")
    @Mapping(target = "created", source = "creationTime")
    @Mapping(target = "pending", source = "numPending")
    @Mapping(target = "waiting", source = "numWaiting")
    @Mapping(target = "acknowledgePending", source = "numAckPending")
    @Mapping(target = "cluster", source = "clusterInfo")
    ConsumerInfo to(io.nats.client.api.ConsumerInfo source);

    default ConsumerConfiguration to(io.nats.client.api.ConsumerConfiguration value,
            @Context io.nats.client.api.ConsumerInfo source) {
        return ConsumerConfigurationRecord.builder()
                .name(value.getName())
                .stream(source.getStreamName())
                .build();
    }
}
