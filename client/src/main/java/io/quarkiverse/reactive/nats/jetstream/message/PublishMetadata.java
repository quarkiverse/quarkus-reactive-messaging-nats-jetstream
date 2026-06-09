package io.quarkiverse.reactive.nats.jetstream.message;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface PublishMetadata extends Metadata {

    static PublishMetadata of(@NonNull String stream,
                              @NonNull String subject,
                              @NonNull String messageId,
                              @Nullable Duration acknowledgeTimeout,
                              @NonNull List<Duration> backoff) {
        return new PublishMetadataRecord(stream, subject, messageId, Optional.ofNullable(acknowledgeTimeout), backoff);
    }

    @NonNull String stream();

    @NonNull String subject();

    @NonNull String messageId();

    @NonNull Optional<Duration> acknowledgeTimeout();

    @NonNull Class<?> getPayloadType();
}
