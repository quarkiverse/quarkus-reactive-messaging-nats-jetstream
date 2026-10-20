package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.opentelemetry.context.propagation.TextMapGetter;

record MessageHeadersTextMapGetter(@NonNull Operation operation) implements TextMapGetter<Message<byte[]>> {

    @SuppressWarnings({ "NullableProblems", "ConstantValue" })
    @Override
    public Iterable<String> keys(Message<byte[]> message) {
        if (message != null) {
            return TracedHeaders.of(operation, message)
                    .map(Map::keySet).orElseGet(Collections::emptySet);
        }
        return Collections.emptyList();
    }

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
