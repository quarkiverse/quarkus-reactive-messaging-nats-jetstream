package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import org.jspecify.annotations.NonNull;

public interface PublishMetadata extends Metadata {

    static PublishMetadata of(@NonNull String stream,
            @NonNull String subject) {
        return new PublishMetadataRecord(stream, subject);
    }

    @NonNull
    String stream();

    @NonNull
    String subject();
}
