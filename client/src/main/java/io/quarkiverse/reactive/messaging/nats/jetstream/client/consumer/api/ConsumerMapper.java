package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(uses = { SequenceMapper.class,
        OptionalMapper.class }, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface ConsumerMapper {

    @Mapping(target = "stream", source = "streamName")
    @Mapping(target = "configuration", expression = "java(io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration.of(source.getConsumerConfiguration()))")
    @Mapping(target = "created", source = "creationTime")
    @Mapping(target = "pending", source = "numPending")
    @Mapping(target = "waiting", source = "numWaiting")
    @Mapping(target = "acknowledgePending", source = "numAckPending")
    ConsumerImpl map(io.nats.client.api.ConsumerInfo source);

}
