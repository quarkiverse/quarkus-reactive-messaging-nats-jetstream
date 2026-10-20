package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record StreamAlternateImpl(@NonNull String name,
        @NonNull Optional<String> domain,
        @NonNull String cluster) implements StreamAlternate {
}
