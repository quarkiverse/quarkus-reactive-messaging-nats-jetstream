package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = { OptionalMapper.class, ObjectMetadataOptionsMapper.class, HeadersMapper.class })
public interface ObjectMetadataMapper {

    @Mapping(target = "options", source = "objectMetaOptions")
    ObjectMetadataImpl map(io.nats.client.api.ObjectMeta value);

    default io.nats.client.api.ObjectMeta map(ObjectMetadata value) {
        final var headersMapper = Mappers.getMapper(HeadersMapper.class);
        var builder = io.nats.client.api.ObjectMeta.builder(value.objectName())
                .description(value.description().orElse(null))
                .headers(headersMapper.map(value.headers()))
                .metadata(value.metadata());
        if (value.options().isPresent()) {
            builder = builder.chunkSize(value.options().get().chunkSize());
            if (value.options().get().link().isPresent()) {
                final var linkMapper = Mappers.getMapper(ObjectLinkMapper.class);
                builder = builder.link(linkMapper.map(value.options().get().link().get()));
            }
        }
        return builder.build();
    }
}
