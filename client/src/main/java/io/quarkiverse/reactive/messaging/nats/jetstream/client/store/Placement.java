package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record Placement(@NonNull Optional<String> cluster, @NonNull List<String> tags) {
    public Placement {
        Objects.requireNonNull(cluster, "cluster");
        Objects.requireNonNull(tags, "tags");
    }
}
