package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ObjectMetadataOptionsImpl(@NonNull Optional<ObjectLink> link,
        @NonNull Integer chunkSize) implements ObjectMetadataOptions {

}
