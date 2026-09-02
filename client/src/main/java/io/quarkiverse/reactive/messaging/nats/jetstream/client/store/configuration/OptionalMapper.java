package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.mapstruct.Mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.ObjectLink;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.ObjectMetadataOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.ConsumerSource;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.External;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Mirror;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Republish;

@Mapper
interface OptionalMapper {

    default Optional<String> map(String value) {
        return Optional.ofNullable(value);
    }

    default Optional<ZonedDateTime> map(ZonedDateTime value) {
        return Optional.ofNullable(value);
    }

    default Optional<ObjectMetadataOptions> map(ObjectMetadataOptions value) {
        return Optional.ofNullable(value);
    }

    default Optional<byte[]> map(byte[] value) {
        return Optional.ofNullable(value);
    }

    default Optional<Duration> map(Duration value) {
        return Optional.ofNullable(value);
    }

    default Optional<ObjectLink> map(ObjectLink value) {
        return Optional.ofNullable(value);
    }

    default Optional<Placement> map(Placement value) {
        return Optional.ofNullable(value);
    }

    default Optional<Mirror> map(Mirror value) {
        return Optional.ofNullable(value);
    }

    default Optional<Republish> map(Republish value) {
        return Optional.ofNullable(value);
    }

    default Optional<External> map(External value) {
        return Optional.ofNullable(value);
    }

    default Optional<ConsumerSource> map(ConsumerSource value) {
        return Optional.ofNullable(value);
    }

    default Optional<Long> map(Long value) {
        return Optional.ofNullable(value).filter(v -> v != -1L);
    }

    default Optional<Integer> map(Integer value) {
        return Optional.ofNullable(value).filter(v -> v != -1);
    }
}
