package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.mapstruct.Mapper;

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

    default Optional<Headers> map(Headers value) {
        return Optional.ofNullable(value);
    }

    default Optional<Placement> map(Placement placement) {
        return Optional.ofNullable(placement);
    }

    default Optional<ObjectLink> map(ObjectLink objectLink) {
        return Optional.ofNullable(objectLink);
    }
}
