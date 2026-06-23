package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {OptionalMapper.class, HeadersMapper.class, ObjectMetadataOptionsMapper.class})
public interface ObjectMetadataMapper {

    @Mapping(target = "options", source = "objectMetaOptions")
    ObjectMetadata map(io.nats.client.api.ObjectMeta metadata);

    default io.nats.client.api.ObjectMeta map(ObjectMetadata metadata) {
        final var headersMapper = Mappers.getMapper(HeadersMapper.class);
        final var objectLinkMapper = Mappers.getMapper(ObjectLinkMapper.class);
        return io.nats.client.api.ObjectMeta.builder(metadata.objectName())
                .metadata(metadata.metadata())
                .description(metadata.description().orElse(null))
                .chunkSize(metadata.options().chunkSize())
                .headers(headersMapper.map(metadata.headers()))
                .link(metadata.options().link().map(objectLinkMapper::map).orElse(null))
                .build();
    }

}
