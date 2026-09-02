package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.mapstruct.Mapper;

@Mapper
public interface OptionalMapper {

    default Optional<ObjectLink> map(ObjectLink value) {
        return Optional.ofNullable(value);
    }

    default Optional<byte[]> map(byte[] value) {
        return Optional.ofNullable(value);
    }

    default Optional<ObjectMetadataOptions> map(ObjectMetadataOptions value) {
        return Optional.ofNullable(value);
    }

    default Optional<String> map(String value) {
        return Optional.ofNullable(value);
    }

    default Optional<ZonedDateTime> map(ZonedDateTime value) {
        return Optional.ofNullable(value);
    }
}
