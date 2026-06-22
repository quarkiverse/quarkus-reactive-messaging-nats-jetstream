package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@Builder
public record ObjectEntry(byte @NonNull [] data, @NonNull ObjectInfo info) {

    public ObjectEntry {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(info, "info");
    }
}
