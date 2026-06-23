package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.util.Objects;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ObjectEntry(byte @NonNull [] data, @NonNull ObjectInfo info) {

    public ObjectEntry {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(info, "info");
    }
}
