package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = { StreamConfigurationMapper.class })
public interface StreamInfoMapper {

    @Mapping(target = "createTime", source = "createTime")
    @Mapping(target = "config", source = "configuration")
    @Mapping(target = "streamState", source = "streamState")
    @Mapping(target = "clusterInfo", source = "clusterInfo")
    @Mapping(target = "mirrorInfo", source = "mirrorInfo")
    @Mapping(target = "sourceInfos", source = "sourceInfos")
    @Mapping(target = "alternates", source = "alternates")
    StreamInfo map(io.nats.client.api.StreamInfo source);

    @Mapping(target = "firstSeq", source = "firstSequence")
    @Mapping(target = "lastSeq", source = "lastSequence")
    @Mapping(target = "consumerCount", source = "consumerCount")
    @Mapping(target = "subjectCount", source = "subjectCount")
    @Mapping(target = "deletedCount", source = "deletedCount")
    @Mapping(target = "firstTime", source = "firstTime")
    @Mapping(target = "lastTime", source = "lastTime")
    @Mapping(target = "subjects", source = "subjects")
    @Mapping(target = "deletedStreamSequences", source = "deleted")
    @Mapping(target = "lostStreamData", source = "lostStreamData")
    @Mapping(target = "subjectMap", source = "subjects")
    @Mapping(target = "msgs", source = "msgCount")
    @Mapping(target = "bytes", source = "byteCount")
    StreamState map(io.nats.client.api.StreamState source);

    @Mapping(target = "trafficAccount", ignore = true)
    ClusterInfo map(io.nats.client.api.ClusterInfo source);

    Replica map(io.nats.client.api.Replica source);

    MirrorInfo map(io.nats.client.api.MirrorInfo source);

    SourceInfo map(io.nats.client.api.SourceInfo source);

    StreamAlternate map(io.nats.client.api.StreamAlternate source);

    LostStreamData map(io.nats.client.api.LostStreamData source);

    Error map(io.nats.client.api.Error source);

    default Subject map(io.nats.client.api.Subject source) {
        return new Subject(source.getName(), source.getCount());
    }

    default Map<String, Long> mapSubjects(List<io.nats.client.api.Subject> subjects) {
        if (subjects == null) {
            return Map.of();
        }
        return subjects.stream().collect(Collectors.toMap(io.nats.client.api.Subject::getName, io.nats.client.api.Subject::getCount));
    }
}
