package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ObjectInfoImpl(@NonNull String bucket,
        @NonNull Optional<String> nuId,
        long size,
        long chunks,
        @NonNull Optional<String> digest,
        boolean deleted,
        @NonNull ObjectMetadata metadata,
        @NonNull Optional<ZonedDateTime> modified) implements ObjectInfo {

}
