package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;

@Mapper(uses = {
        OptionalMapper.class,
        LostStreamDataMapper.class,
        StreamStateMapper.class,
        ClusterMapper.class,
        MirrorMapper.class,
        SourceMapper.class,
        StreamAlternateMapper.class }, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface StreamMapper {

    @Mapping(target = "created", source = "createTime")
    @Mapping(target = "configuration", expression = "java(map(source.getConfiguration()))")
    @Mapping(target = "cluster", source = "clusterInfo")
    @Mapping(target = "mirror", source = "mirrorInfo")
    @Mapping(target = "sources", source = "sourceInfos")
    StreamImpl map(io.nats.client.api.StreamInfo source);

    default StreamConfiguration map(io.nats.client.api.StreamConfiguration source) {
        return StreamConfiguration.of(source);
    }
}
