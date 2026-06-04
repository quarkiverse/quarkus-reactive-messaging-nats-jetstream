package io.quarkiverse.reactive.nats.jetstream.message;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Builder
record PublishMetadataRecord(@NonNull String stream,
                             @NonNull String subject,
                             @NonNull String messageId,
                             @NonNull Headers headers,
                             @NonNull Optional<Duration> acknowledgeTimeout,
                             @NonNull List<Duration> backoff) implements PublishMetadata {
}
