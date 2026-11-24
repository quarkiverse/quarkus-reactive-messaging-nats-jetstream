package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;

record MessageHeadersTextMapGetter(@NonNull Operation operation) implements TextMapGetter<Message<byte[]>> {

    @SuppressWarnings("NullableProblems")
    @Override
    public String get(@Nullable Message<byte[]> message, String key) {
        if (message != null) {
            return TracedHeaders.of(operation, message)
                    .flatMap(headers -> Optional.ofNullable(headers.get(key)))
                    .map(values -> String.join(",", values))
                    .orElse(null);
        }
        return null;
    }
}
