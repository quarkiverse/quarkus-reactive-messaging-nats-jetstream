package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ObjectLink(@NonNull String bucket, @NonNull Optional<String> objectName) {
    public ObjectLink {
        Objects.requireNonNull(bucket, "bucket");
        Objects.requireNonNull(objectName, "objectName");
    }
}
