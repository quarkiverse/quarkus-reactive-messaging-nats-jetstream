package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.time.ZonedDateTime;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ObjectInfo(@NonNull String bucket,
        @NonNull String nuId,
        @NonNull Long size,
        @NonNull Long chunks,
        @NonNull String digest,
        @NonNull Boolean deleted,
        @NonNull ObjectMetadata metadata,
        @NonNull ZonedDateTime modified) {

    public ObjectInfo {
        Objects.requireNonNull(bucket, "bucket");
        Objects.requireNonNull(nuId, "nuId");
        Objects.requireNonNull(size, "size");
        Objects.requireNonNull(chunks, "chunks");
        Objects.requireNonNull(digest, "digest");
        Objects.requireNonNull(deleted, "deleted");
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(modified, "modified");
    }
}
