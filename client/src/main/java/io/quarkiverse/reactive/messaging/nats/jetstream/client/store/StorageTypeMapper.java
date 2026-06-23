package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;

@Mapper
interface StorageTypeMapper {

    io.nats.client.api.StorageType map(StorageType storageType);
}
