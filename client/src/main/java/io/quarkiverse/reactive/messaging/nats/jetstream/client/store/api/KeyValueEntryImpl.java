package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.nats.client.api.KeyValueOperation;
import lombok.Builder;

@Builder
public record KeyValueEntryImpl(@NonNull String bucket,
        @NonNull String key,
        @NonNull Optional<byte[]> value,
        long dataLength,
        @NonNull ZonedDateTime created,
        long revision,
        long delta,
        @NonNull KeyValueOperation operation) implements KeyValueEntry {

}
