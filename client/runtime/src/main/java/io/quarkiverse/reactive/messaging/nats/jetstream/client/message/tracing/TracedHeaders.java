package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.*;

final class TracedHeaders {

    private TracedHeaders() {
    }

    /**
     * A message being published carries {@link PublishHeaders}; a received message carries {@link MessageHeaders}. A
     * reply built from a received message carries both, so the operation decides which ones are traced.
     */
    static @NonNull Optional<Headers> of(@NonNull Operation operation, @NonNull Message<byte[]> message) {
        return switch (operation) {
            case PUBLISH -> message.getMetadata(PublishHeaders.class).map(Headers.class::cast);
            case RECEIVE, PROCESS -> message.getMetadata(MessageHeaders.class).map(Headers.class::cast);
        };
    }

    /**
     * The stream of a received message comes from its JetStream delivery metadata, which is always present; the
     * stream header is only set by this extension's publisher and is the only source when publishing.
     */
    static @NonNull Optional<String> stream(@NonNull Message<byte[]> message, @NonNull Headers headers) {
        return message.getMetadata(MessageMetadata.class).map(MessageMetadata::stream).or(headers::stream);
    }
}
