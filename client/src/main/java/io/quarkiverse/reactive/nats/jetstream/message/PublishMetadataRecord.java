package io.quarkiverse.reactive.nats.jetstream.message;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
record PublishMetadataRecord(@NonNull String stream,
        @NonNull String subject,
        @NonNull String messageId,
        @NonNull Optional<Duration> acknowledgeTimeout,
        @NonNull List<Duration> backoff) implements PublishMetadata {
}
