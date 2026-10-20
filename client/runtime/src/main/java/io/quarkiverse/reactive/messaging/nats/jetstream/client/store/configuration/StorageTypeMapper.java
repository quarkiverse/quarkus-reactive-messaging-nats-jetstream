package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import org.mapstruct.Mapper;

@Mapper
interface StorageTypeMapper {

    io.nats.client.api.StorageType map(StorageType storageType);
}
