package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ObjectLinkImpl(@NonNull String bucket, @NonNull Optional<String> objectName) implements ObjectLink {

    @Override
    public boolean isObjectLink() {
        return objectName.isPresent();
    }

    @Override
    public boolean isBucketLink() {
        return objectName.isEmpty();
    }
}
