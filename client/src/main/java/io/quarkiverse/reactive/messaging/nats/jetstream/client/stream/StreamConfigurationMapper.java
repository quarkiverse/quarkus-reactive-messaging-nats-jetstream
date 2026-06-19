package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;
import java.util.List;

@Mapper
public interface StreamConfigurationMapper {

    @Mapping(target = "maxMessagesPerSubject", source = "maxMsgsPerSubject")
    @Mapping(target = "maxMessages", source = "maxMsgs")
    @Mapping(target = "maximumMessageSize", source = "maxMsgSize")
    @Mapping(target = "allowMessageSchedules", source = "allowMsgSchedules")
    StreamConfiguration map(io.nats.client.api.StreamConfiguration source);

    @Mapping(target = "maxMsgSize", source = "maximumMessageSize")
    io.nats.client.api.StreamConfiguration map(StreamConfiguration source);

/**
    String description,
    RetentionPolicy retentionPolicy,
    CompressionOption compressionOption,
    long maxConsumers,
    long maxMessages,
    long maxMessagesPerSubject,
    long maxBytes,
    Duration maxAge,
    int maximumMessageSize,
    StorageType storageType,
    int replicas,
    boolean noAck,
    String templateOwner,
    DiscardPolicy discardPolicy,
    Duration duplicateWindow,
    Placement placement,
    Republish republish,
    SubjectTransform subjectTransform,
    ConsumerLimits consumerLimits,
    Mirror mirror,
    List<Source> sources,
    boolean sealed,
    boolean allowRollup,
    boolean allowDirect,
    boolean mirrorDirect,
    boolean denyDelete,
    boolean denyPurge,
    boolean discardNewPerSubject,
    Map<String, String> metadata,
    long firstSequence,
    Duration subjectDeleteMarkerTtl,
    boolean allowMessageTtl,
    boolean allowMessageSchedules,
    boolean allowMessageCounter,
    boolean allowAtomicPublish,
    boolean allowBatched,
    PersistMode persistMode) {*/

    @Mapping(target = "subjects", source = "subjects")
    @Mapping(target = "description", source = "source.description")
    StreamConfiguration map(StreamConfiguration source, List<String> subjects);

    default long map(Duration value) {
        return value.toNanos();
    }

    default io.nats.client.api.Mirror map(Mirror value) {
        return io.nats.client.api.Mirror.builder()
                .name(value.name())
                .consumerSource(map(value.consumerSource()))
                .external(map(value.external()))
                .filterSubject(value.filterSubject())
                .startSeq(value.startSeq())
                .startTime(value.startTime())
                .subjectTransforms(value.subjectTransforms().stream().map(this::map).toList())
                .build();
    }

    io.nats.client.api.ConsumerSource map(ConsumerSource value);

    io.nats.client.api.External map(External value);

    io.nats.client.api.SubjectTransform map(SubjectTransform value);

    io.nats.client.api.Source map(Source value);
}
