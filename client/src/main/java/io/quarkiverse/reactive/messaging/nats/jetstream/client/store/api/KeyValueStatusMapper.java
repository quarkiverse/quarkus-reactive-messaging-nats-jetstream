package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface KeyValueStatusMapper {

    @Mapping(target = "stream", expression = "java(io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream.of(status.getBackingStreamInfo()))")
    @Mapping(target = "configuration", expression = "java(io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.KeyValueConfiguration.of(status.getConfiguration()))")
    KeyValueStatusImpl map(io.nats.client.api.KeyValueStatus status);
}
