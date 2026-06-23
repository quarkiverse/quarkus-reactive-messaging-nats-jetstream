package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = { OptionalMapper.class, HeadersMapper.class, ObjectMetadataMapper.class })
public interface ObjectInfoMapper {

    @Mapping(target = "nuId", source = "nuid")
    @Mapping(target = "metadata", source = "objectMeta")
    ObjectInfo map(io.nats.client.api.ObjectInfo objectInfo);

    default io.nats.client.api.ObjectInfo map(ObjectInfo objectInfo) {
        final var headersMapper = Mappers.getMapper(HeadersMapper.class);
        final var builder = io.nats.client.api.ObjectInfo.builder(objectInfo.bucket(), objectInfo.metadata().objectName())
                .nuid(objectInfo.nuId())
                .size(objectInfo.size())
                .headers(headersMapper.map(objectInfo.metadata().headers()))
                .description(objectInfo.metadata().description().orElse(null))
                .chunkSize(objectInfo.metadata().options().chunkSize())
                .chunks(objectInfo.chunks())
                .digest(objectInfo.digest())
                .deleted(objectInfo.deleted())
                .modified(objectInfo.modified())
                .metadata(objectInfo.metadata().metadata());
        if (objectInfo.metadata().options().link().isPresent()) {
            final var objectLink = objectInfo.metadata().options().link().get();
            if (objectLink.objectName().isPresent()) {
                builder.objectLink(objectLink.bucket(), objectLink.objectName().get());
            } else {
                builder.bucketLink(objectLink.bucket());
            }
        }
        return builder.build();
    }
}
