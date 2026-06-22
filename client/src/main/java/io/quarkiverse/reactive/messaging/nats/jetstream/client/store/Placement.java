package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

@Builder
public record Placement(@NonNull String cluster, @NonNull List<String> tags) {
    public Placement {
        Objects.requireNonNull(cluster, "cluster");
        Objects.requireNonNull(tags, "tags");
    }
}
