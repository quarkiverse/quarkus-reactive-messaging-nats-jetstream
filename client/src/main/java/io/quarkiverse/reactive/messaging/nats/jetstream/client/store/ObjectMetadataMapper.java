package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper
public interface ObjectMetadataMapper {

    io.nats.client.api.ObjectMeta map(ObjectMetadata metadata);

    @ObjectFactory
    default io.nats.client.api.ObjectMeta create(ObjectMetadata metadata) {
        return io.nats.client.api.ObjectMeta.builder(metadata.objectName())
                .metadata(metadata.metadata())
                .description(metadata.description().orElse(null))
                .chunkSize(metadata.metadataOptions().chunkSize())
                .build();
    }

}
