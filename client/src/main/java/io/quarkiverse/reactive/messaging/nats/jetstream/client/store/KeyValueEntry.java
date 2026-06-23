package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.time.ZonedDateTime;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import io.nats.client.api.KeyValueOperation;
import lombok.Builder;

@Builder
public record KeyValueEntry(@NonNull String bucket,
        @NonNull String key,
        byte @NonNull [] value,
        long dataLength,
        @NonNull ZonedDateTime created,
        long revision,
        long delta,
        @NonNull KeyValueOperation operation) {

    public KeyValueEntry {
        Objects.requireNonNull(bucket, "bucket");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(created, "created");
        Objects.requireNonNull(operation, "operation");
    }
}
