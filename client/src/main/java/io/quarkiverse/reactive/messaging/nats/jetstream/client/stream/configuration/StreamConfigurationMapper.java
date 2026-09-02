package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.util.List;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
        ConsumerLimitsMapper.class,
        PlacementMapper.class,
        RepublishMapper.class,
        SubjectTransformMapper.class,
        MirrorMapper.class,
        OptionalMapper.class,
        DiscardPolicyMapper.class,
        ExternalMapper.class,
        ConsumerSourceMapper.class,
        SourceMapper.class,
        RetentionPolicyMapper.class,
        CompressionMapper.class,
        StorageTypeMapper.class,
        PersistModeMapper.class }, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface StreamConfigurationMapper {

    @Mapping(target = "name", expression = "java(source.name())")
    @Mapping(target = "description", expression = "java(source.description().orElse(null))")
    @Mapping(target = "subjects", expression = "java(source.subjects())")
    @Mapping(target = "retentionPolicy", expression = "java(retentionPolicyMapper.map(source.retentionPolicy()))")
    @Mapping(target = "compressionOption", expression = "java(compressionMapper.map(source.compression()))")
    @Mapping(target = "maxConsumers", expression = "java(source.maxConsumers().orElse(-1L))")
    @Mapping(target = "maxMessages", expression = "java(source.maxMessages().orElse(-1L))")
    @Mapping(target = "maxMessagesPerSubject", expression = "java(source.maxMessagesPerSubject().orElse(-1L))")
    @Mapping(target = "maxBytes", expression = "java(source.maxBytes().orElse(-1L))")
    @Mapping(target = "maxAge", expression = "java(source.maxAge().orElse(java.time.Duration.ZERO))")
    @Mapping(target = "maxMsgSize", ignore = true)
    @Mapping(target = "maximumMessageSize", expression = "java(source.maximumMessageSize().orElse(-1))")
    @Mapping(target = "storageType", expression = "java(storageTypeMapper.map(source.storageType()))")
    @Mapping(target = "replicas", expression = "java(source.replicas())")
    @Mapping(target = "noAck", constant = "false")
    @Mapping(target = "templateOwner", expression = "java(source.templateOwner().orElse(null))")
    @Mapping(target = "discardPolicy", expression = "java(Mappers.getMapper(DiscardPolicyMapper.class).map(source.discardPolicy()))")
    @Mapping(target = "duplicateWindow", expression = "java(source.duplicateWindow().orElse(java.time.Duration.ZERO))")
    @Mapping(target = "placement", expression = "java(source.placement().map(Mappers.getMapper(PlacementMapper.class)::map).orElse(null))")
    @Mapping(target = "republish", expression = "java(source.republish().map(republishMapper::map).orElse(null))")
    @Mapping(target = "consumerLimits", expression = "java(source.consumerLimits().map(consumerLimitsMapper::map).orElse(null))")
    @Mapping(target = "mirror", expression = "java(source.mirror().map(mirrorMapper::map).orElse(null))")
    @Mapping(target = "allowRollup", expression = "java(source.allowRollup())")
    @Mapping(target = "allowDirect", expression = "java(source.allowDirect())")
    @Mapping(target = "mirrorDirect", expression = "java(source.mirrorDirect())")
    @Mapping(target = "denyDelete", expression = "java(source.denyDelete())")
    @Mapping(target = "denyPurge", expression = "java(source.denyPurge())")
    @Mapping(target = "discardNewPerSubject", expression = "java(source.discardNewPerSubject())")
    @Mapping(target = "metadata", expression = "java(source.metadata())")
    @Mapping(target = "firstSequence", expression = "java(source.firstSequence())")
    @Mapping(target = "subjectDeleteMarkerTtl", expression = "java(source.subjectDeleteMarkerTtl().orElse(null))")
    @Mapping(target = "allowMessageTtl", expression = "java(source.allowMessageTtl())")
    @Mapping(target = "allowMessageSchedules", expression = "java(source.allowMessageSchedules())")
    @Mapping(target = "allowMessageCounter", expression = "java(source.allowMessageCounter())")
    @Mapping(target = "allowAtomicPublish", expression = "java(source.allowAtomicPublish())")
    @Mapping(target = "allowBatched", expression = "java(source.allowBatched())")
    @Mapping(target = "persistMode", expression = "java(source.persistMode().map(persistModeMapper::map).orElse(null))")
    @Mapping(target = "sources", expression = "java(sources(source))")
    @Mapping(target = "subjectTransform", expression = "java(source.subjectTransform().map(subjectTransformMapper::map).orElse(null))")
    io.nats.client.api.StreamConfiguration map(StreamConfiguration source);

    @Mapping(target = "compression", source = "compressionOption")
    @Mapping(target = "maxMessages", source = "maxMsgs")
    @Mapping(target = "maxMessagesPerSubject", source = "maxMsgsPerSubject")
    @Mapping(target = "allowMessageSchedules", source = "allowMsgSchedules")
    @Mapping(target = "placement", expression = "java(java.util.Optional.ofNullable(value.getPlacement()).map(Mappers.getMapper(PlacementMapper.class)::map).map(placement -> (Placement) placement))")
    @Mapping(target = "metadata", expression = "java(java.util.Optional.ofNullable(value.getMetadata()).orElseGet(java.util.Map::of))")
    @Mapping(target = "sources", expression = "java(sources(value))")
    StreamConfigurationImpl map(io.nats.client.api.StreamConfiguration value);

    @Mapping(target = "name", expression = "java(source.name())")
    @Mapping(target = "description", expression = "java(source.description())")
    @Mapping(target = "subjects", source = "subjects")
    @Mapping(target = "retentionPolicy", expression = "java(source.retentionPolicy())")
    @Mapping(target = "compression", expression = "java(source.compression())")
    @Mapping(target = "maxConsumers", expression = "java(source.maxConsumers())")
    @Mapping(target = "maxMessages", expression = "java(source.maxMessages())")
    @Mapping(target = "maxMessagesPerSubject", expression = "java(source.maxMessagesPerSubject())")
    @Mapping(target = "maxBytes", expression = "java(source.maxBytes())")
    @Mapping(target = "maxAge", expression = "java(source.maxAge())")
    @Mapping(target = "maximumMessageSize", expression = "java(source.maximumMessageSize())")
    @Mapping(target = "storageType", expression = "java(source.storageType())")
    @Mapping(target = "replicas", expression = "java(source.replicas())")
    @Mapping(target = "noAck", constant = "false")
    @Mapping(target = "templateOwner", expression = "java(source.templateOwner())")
    @Mapping(target = "discardPolicy", expression = "java(source.discardPolicy())")
    @Mapping(target = "duplicateWindow", expression = "java(source.duplicateWindow())")
    @Mapping(target = "placement", expression = "java(source.placement().map(Mappers.getMapper(PlacementMapper.class)::to).map(placement -> (Placement) placement))")
    @Mapping(target = "republish", expression = "java(source.republish().map(republishMapper::to))")
    @Mapping(target = "consumerLimits", expression = "java(source.consumerLimits().map(consumerLimitsMapper::to))")
    @Mapping(target = "mirror", expression = "java(source.mirror().map(mirrorMapper::to))")
    @Mapping(target = "allowRollup", expression = "java(source.allowRollup())")
    @Mapping(target = "allowDirect", expression = "java(source.allowDirect())")
    @Mapping(target = "mirrorDirect", expression = "java(source.mirrorDirect())")
    @Mapping(target = "denyDelete", expression = "java(source.denyDelete())")
    @Mapping(target = "denyPurge", expression = "java(source.denyPurge())")
    @Mapping(target = "discardNewPerSubject", expression = "java(source.discardNewPerSubject())")
    @Mapping(target = "metadata", expression = "java(source.metadata())")
    @Mapping(target = "firstSequence", expression = "java(source.firstSequence())")
    @Mapping(target = "subjectDeleteMarkerTtl", expression = "java(source.subjectDeleteMarkerTtl())")
    @Mapping(target = "allowMessageTtl", expression = "java(source.allowMessageTtl())")
    @Mapping(target = "allowMessageSchedules", expression = "java(source.allowMessageSchedules())")
    @Mapping(target = "allowMessageCounter", expression = "java(source.allowMessageCounter())")
    @Mapping(target = "allowAtomicPublish", expression = "java(source.allowAtomicPublish())")
    @Mapping(target = "allowBatched", expression = "java(source.allowBatched())")
    @Mapping(target = "persistMode", expression = "java(source.persistMode())")
    @Mapping(target = "sources", expression = "java(source.sources())")
    @Mapping(target = "subjectTransform", expression = "java(source.subjectTransform().map(subjectTransformMapper::to))")
    @Mapping(target = "sealed", expression = "java(source.sealed())")
    StreamConfigurationImpl map(StreamConfiguration source, List<String> subjects);

    default Optional<DiscardPolicy> map(io.nats.client.api.DiscardPolicy value) {
        final var mapper = Mappers.getMapper(DiscardPolicyMapper.class);
        return value != null ? Optional.of(mapper.map(value)) : Optional.empty();
    }

    default List<Source> sources(io.nats.client.api.StreamConfiguration value) {
        if (value == null || value.getSources() == null)
            return List.of();
        final var mapper = Mappers.getMapper(SourceMapper.class);
        return value.getSources().stream().map(mapper::map).map(source -> (Source) source).toList();
    }

    default List<io.nats.client.api.Source> sources(StreamConfiguration value) {
        if (value == null)
            return List.of();
        final var mapper = Mappers.getMapper(SourceMapper.class);
        return value.sources().stream().map(mapper::map).toList();
    }
}
