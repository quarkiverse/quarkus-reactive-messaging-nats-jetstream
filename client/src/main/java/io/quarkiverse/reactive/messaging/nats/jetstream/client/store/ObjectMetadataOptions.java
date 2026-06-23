package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;

@Builder
public record ObjectMetadataOptions(@NonNull Optional<ObjectLink> link, @NonNull Integer chunkSize) {

    public ObjectMetadataOptions {
        Objects.requireNonNull(link, "link");
        Objects.requireNonNull(chunkSize, "chunkSize");
    }
}
