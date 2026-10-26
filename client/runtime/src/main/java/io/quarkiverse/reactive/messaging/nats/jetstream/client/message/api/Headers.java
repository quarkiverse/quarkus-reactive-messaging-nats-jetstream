package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.api;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface Headers extends io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Headers {

    static @NonNull Headers of(io.nats.client.impl.@Nullable Headers headers) {
        final var result = new HeadersImpl();
        if (headers != null) {
            headers.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    static @NonNull Headers of() {
        return new HeadersImpl();
    }
}
