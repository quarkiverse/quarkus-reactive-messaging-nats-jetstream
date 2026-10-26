package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
record RepublishImpl(@NonNull String source,
        @NonNull String destination,
        boolean headersOnly) implements Republish {
}
