package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ObjectMetadataImpl(@NonNull String objectName,
        @NonNull Optional<String> description,
        @NonNull Map<String, List<String>> headers,
        @NonNull Map<String, String> metadata,
        @NonNull Optional<ObjectMetadataOptions> options) implements ObjectMetadata {

}
