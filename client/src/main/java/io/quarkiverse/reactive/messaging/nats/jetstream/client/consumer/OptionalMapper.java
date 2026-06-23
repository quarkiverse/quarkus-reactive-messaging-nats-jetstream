package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import org.mapstruct.Mapper;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@Mapper
interface OptionalMapper {

    default Optional<Duration> map(Duration duration) {
        return Optional.ofNullable(duration);
    }

    default Optional<Long> map(Long value) {
        return Optional.ofNullable(value);
    }

    default Optional<Boolean> map(Boolean value) {
        return Optional.ofNullable(value);
    }

    default Optional<ZonedDateTime> map(ZonedDateTime value) {
        return Optional.ofNullable(value);
    }

    default Optional<Integer> map(Integer value) {
        return Optional.ofNullable(value);
    }

    default Optional<String> map(String value) {
        return Optional.ofNullable(value);
    }

    default Map<String, String> map(Map<String, String> map) {
        return map == null ? Map.of() : map;
    }
}
