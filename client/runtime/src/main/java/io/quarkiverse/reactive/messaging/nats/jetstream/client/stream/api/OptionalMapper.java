package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.External;

@Mapper
interface OptionalMapper {

    default Optional<String> map(@Nullable String value) {
        return Optional.ofNullable(value);
    }

    default Optional<ZonedDateTime> map(@Nullable ZonedDateTime value) {
        return Optional.ofNullable(value);
    }

    default Optional<Duration> map(@Nullable Duration value) {
        return Optional.ofNullable(value);
    }

    default Optional<Cluster> map(@Nullable Cluster value) {
        return Optional.ofNullable(value);
    }

    default Optional<LostStreamData> map(LostStreamData value) {
        return Optional.ofNullable(value);
    }

    default Optional<Error> map(Error value) {
        return Optional.ofNullable(value);
    }

    default Optional<Mirror> map(Mirror value) {
        return Optional.ofNullable(value);
    }

    default Optional<Long> map(@Nullable Long value) {
        return Optional.ofNullable(value);
    }

    default Optional<External> map(@Nullable External value) {
        return Optional.ofNullable(value);
    }

}
