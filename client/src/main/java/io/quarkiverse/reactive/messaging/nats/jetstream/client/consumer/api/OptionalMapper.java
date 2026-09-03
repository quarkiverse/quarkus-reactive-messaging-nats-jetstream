package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.mapstruct.Mapper;

@Mapper
public interface OptionalMapper {

    default Optional<ZonedDateTime> map(ZonedDateTime source) {
        return Optional.ofNullable(source);
    }

    default Optional<Duration> map(Duration source) {
        return Optional.ofNullable(source);
    }
}
