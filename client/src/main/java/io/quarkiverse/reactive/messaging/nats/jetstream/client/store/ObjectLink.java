package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@Builder
public record ObjectLink(@NonNull String bucket, @NonNull String objectName) {
    public ObjectLink {
        Objects.requireNonNull(bucket, "bucket");
        Objects.requireNonNull(objectName, "objectName");
    }
}
