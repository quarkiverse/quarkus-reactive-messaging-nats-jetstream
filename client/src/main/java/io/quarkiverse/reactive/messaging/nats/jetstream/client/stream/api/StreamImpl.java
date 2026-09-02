package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;
import lombok.Builder;

@Builder
public record StreamImpl(@NonNull StreamConfiguration configuration,
        @NonNull ZonedDateTime created,
        @NonNull StreamState streamState,
        @NonNull Optional<Cluster> cluster,
        @NonNull Optional<Mirror> mirror,
        @NonNull List<Source> sources,
        @NonNull List<StreamAlternate> alternates,
        @NonNull Optional<ZonedDateTime> timestamp) implements Stream {

}
