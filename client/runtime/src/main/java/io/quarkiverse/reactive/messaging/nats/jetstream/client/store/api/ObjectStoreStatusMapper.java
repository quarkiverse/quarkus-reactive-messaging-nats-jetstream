package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ObjectStoreStatusMapper {

    @Mapping(target = "stream", expression = "java(io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream.of(status.getBackingStreamInfo()))")
    @Mapping(target = "configuration", expression = "java(io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.ObjectStoreConfiguration.of(status.getConfiguration()))")
    ObjectStoreStatusImpl map(io.nats.client.api.ObjectStoreStatus status);

}
