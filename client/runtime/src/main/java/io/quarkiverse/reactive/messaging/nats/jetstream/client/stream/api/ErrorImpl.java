package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ErrorImpl(int code, int apiErrorCode, @NonNull String description) implements Error {
}
