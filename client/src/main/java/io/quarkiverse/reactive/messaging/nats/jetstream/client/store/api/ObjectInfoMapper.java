package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = { OptionalMapper.class, ObjectMetadataMapper.class })
public interface ObjectInfoMapper {

    @Mapping(target = "nuId", source = "nuid")
    @Mapping(target = "metadata", source = "objectMeta")
    ObjectInfoImpl map(io.nats.client.api.ObjectInfo objectInfo);

    default io.nats.client.api.ObjectInfo map(ObjectInfo objectInfo) {
        final var mapper = Mappers.getMapper(ObjectMetadataMapper.class);
        return io.nats.client.api.ObjectInfo
                .builder(objectInfo.bucket(), mapper.map(objectInfo.metadata()))
                .nuid(objectInfo.nuId().orElse(null))
                .size(objectInfo.size())
                .modified(objectInfo.modified().orElse(null))
                .chunks(objectInfo.chunks())
                .digest(objectInfo.digest().orElse(null))
                .deleted(objectInfo.deleted())
                .build();
    }
}
