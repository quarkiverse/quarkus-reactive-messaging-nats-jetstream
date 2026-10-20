package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
record PlacementImpl(@NonNull Optional<String> cluster, @NonNull List<String> tags) implements Placement {
}
