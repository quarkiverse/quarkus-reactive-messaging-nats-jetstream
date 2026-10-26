package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper
public interface OptionalMapper {

    default Optional<String> map(@Nullable String value) {
        return Optional.ofNullable(value);
    }

    default Optional<ZonedDateTime> map(@Nullable ZonedDateTime value) {
        return Optional.ofNullable(value);
    }

    default Optional<Duration> map(@Nullable Duration value) {
        return Optional.ofNullable(value);
    }

    default Optional<External> map(@Nullable External value) {
        return Optional.ofNullable(value);
    }

    default Optional<ConsumerSource> map(@Nullable ConsumerSource value) {
        return Optional.ofNullable(value);
    }

    default Optional<DiscardPolicy> map(@Nullable DiscardPolicy discardPolicy) {
        return Optional.ofNullable(discardPolicy);
    }

    default Optional<Placement> map(Placement value) {
        return Optional.ofNullable(value);
    }

    default Optional<Republish> map(Republish value) {
        return Optional.ofNullable(value);
    }

    default Optional<SubjectTransform> map(SubjectTransform value) {
        return Optional.ofNullable(value);
    }

    default Optional<ConsumerLimits> map(ConsumerLimits value) {
        return Optional.ofNullable(value);
    }

    default Optional<Mirror> map(Mirror value) {
        return Optional.ofNullable(value);
    }

    default Optional<Long> map(@Nullable Long value) {
        return Optional.ofNullable(value).filter(v -> v != -1L);
    }

    default Optional<Integer> map(@Nullable Integer value) {
        return Optional.ofNullable(value).filter(v -> v != -1);
    }

    default Optional<PersistMode> map(@Nullable PersistMode value) {
        return Optional.ofNullable(value);
    }
}
