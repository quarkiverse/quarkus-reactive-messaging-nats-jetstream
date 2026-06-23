package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record Error(int code, int apiErrorCode, @NonNull String description) {
}
