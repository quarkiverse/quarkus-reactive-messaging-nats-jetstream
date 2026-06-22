package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@Builder
public record ObjectMetadataOptions(@NonNull ObjectLink link, @NonNull Integer chunkSize) {

    public ObjectMetadataOptions {
        Objects.requireNonNull(link, "link");
        Objects.requireNonNull(chunkSize, "chunkSize");
    }
}
