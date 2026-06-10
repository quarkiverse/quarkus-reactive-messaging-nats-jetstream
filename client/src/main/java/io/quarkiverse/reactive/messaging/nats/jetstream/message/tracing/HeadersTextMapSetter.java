package io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing;

import java.util.List;

import io.opentelemetry.context.propagation.TextMapSetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Headers;
import org.eclipse.microprofile.reactive.messaging.Message;

class HeadersTextMapSetter<T> implements TextMapSetter<Message<T>> {

    @Override
    public void set(Message<T> message, final String key, final String value) {
        if (message != null) {
            final var headers = message.getMetadata(Headers.class).orElseThrow(() -> new IllegalStateException("Headers not found"));
            headers.put(key, List.of(value));
        }
    }
}
