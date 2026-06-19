package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record Error(int code, int apiErrorCode, @NonNull String description) {
}
