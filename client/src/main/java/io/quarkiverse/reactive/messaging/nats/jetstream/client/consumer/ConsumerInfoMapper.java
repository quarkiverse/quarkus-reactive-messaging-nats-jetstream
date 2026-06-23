package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = { ConsumerConfigurationMapper.class })
public interface ConsumerInfoMapper {

    @Mapping(target = "stream", source = "streamName")
    @Mapping(target = "configuration", source = "consumerConfiguration")
    @Mapping(target = "created", source = "creationTime")
    @Mapping(target = "pending", source = "numPending")
    @Mapping(target = "waiting", source = "numWaiting")
    @Mapping(target = "acknowledgePending", source = "numAckPending")
    @Mapping(target = "cluster", source = "clusterInfo")
    ConsumerInfo to(io.nats.client.api.ConsumerInfo source);

    private Optional<Duration> mapDuration(Duration duration) {
        return Optional.ofNullable(duration);
    }

    private Optional<Long> mapLong(Long value) {
        return Optional.ofNullable(value);
    }

    private Optional<Boolean> mapBoolean(Boolean value) {
        return Optional.ofNullable(value);
    }

    private Optional<ZonedDateTime> mapZonedDateTime(ZonedDateTime value) {
        return Optional.ofNullable(value);
    }

    private Optional<Integer> mapInteger(Integer value) {
        return Optional.ofNullable(value);
    }

    private Optional<String> mapString(String value) {
        return Optional.ofNullable(value);
    }

    private List<Duration> mapDurationList(Duration[] durations) {
        return durations == null ? List.of() : List.of(durations);
    }

    private Map<String, String> mapMetadata(Map<String, String> metadata) {
        return metadata == null ? Map.of() : metadata;
    }
}
