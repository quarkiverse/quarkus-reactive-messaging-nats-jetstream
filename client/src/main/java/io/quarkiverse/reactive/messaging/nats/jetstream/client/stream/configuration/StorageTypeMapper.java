package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;

@Mapper
interface StorageTypeMapper {

    StorageType map(io.nats.client.api.StorageType source);

    io.nats.client.api.StorageType map(StorageType republish);

}
