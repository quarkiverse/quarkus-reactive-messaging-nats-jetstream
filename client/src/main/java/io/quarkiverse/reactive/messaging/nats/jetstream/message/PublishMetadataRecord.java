package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
record PublishMetadataRecord(@NonNull String stream, @NonNull String subject) implements PublishMetadata {
}
