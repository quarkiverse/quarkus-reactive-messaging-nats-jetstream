package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Builder
public record ObjectMetadata(@NonNull String objectName,
                             @NonNull Optional<String> description,
                             @NonNull Headers headers,
                             @NonNull Map<String, String> metadata,
                             @NonNull ObjectMetadataOptions metadataOptions) {
    public ObjectMetadata {
        Objects.requireNonNull(objectName, "objectName");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(headers, "headers");
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(metadataOptions, "metadataOptions");
    }
}
