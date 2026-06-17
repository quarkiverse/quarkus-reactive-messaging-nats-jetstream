package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ConsumerConfigurationMapper {

    @Mapping(target = "name", source = "name")
    @Mapping(target = "durable", source = "durable")
    @Mapping(target = "filterSubject", source = "filterSubject")
    @Mapping(target = "filterSubjects", source = "filterSubjects")
    @Mapping(target = "acknowledgeWait", source = "ackWait")
    @Mapping(target = "deliverPolicy", source = "deliverPolicy")
    @Mapping(target = "startSequence", source = "startSequence")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "inactiveThreshold", source = "inactiveThreshold")
    @Mapping(target = "maxAcknowledgePending", source = "maxAckPending")
    @Mapping(target = "maxDeliver", source = "maxDeliver")
    @Mapping(target = "replayPolicy", source = "replayPolicy")
    @Mapping(target = "replicas", source = "numReplicas")
    @Mapping(target = "memoryStorage", source = "memStorage")
    @Mapping(target = "sampleFrequency", source = "sampleFrequency")
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "backoff", source = "backoff")
    @Mapping(target = "pauseUntil", source = "pauseUntil")
    @Mapping(target = "headersOnly", source = "headersOnly")
    @Mapping(target = "acknowledgeTimeout", ignore = true)
    @Mapping(target = "pullConfiguration", source = ".")
    ConsumerConfiguration map(io.nats.client.api.ConsumerConfiguration source);

    default Optional<Duration> mapDuration(Duration duration) {
        return Optional.ofNullable(duration);
    }

    default Optional<Long> mapLong(Long value) {
        return Optional.ofNullable(value);
    }

    default Optional<Boolean> mapBoolean(Boolean value) {
        return Optional.ofNullable(value);
    }

    default Optional<ZonedDateTime> mapZonedDateTime(ZonedDateTime value) {
        return Optional.ofNullable(value);
    }

    default Optional<Integer> mapInteger(Integer value) {
        return Optional.ofNullable(value);
    }

    default Optional<String> mapString(String value) {
        return Optional.ofNullable(value);
    }

    default List<Duration> mapDurationList(Duration[] durations) {
        return durations == null ? List.of() : List.of(durations);
    }

    default Map<String, String> mapMetadata(Map<String, String> metadata) {
        return metadata == null ? Map.of() : metadata;
    }

    default PullConfiguration mapPull(io.nats.client.api.ConsumerConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        return PullConfiguration.builder()
                .maxWaiting(mapLong(configuration.getMaxPullWaiting()))
                .maxRequestExpires(mapDuration(configuration.getMaxExpires()))
                .maxRequestBatch(mapLong(configuration.getMaxBatch()))
                .maxRequestMaxBytes(mapLong(configuration.getMaxBytes()))
                .build();
    }
}
