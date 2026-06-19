package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.util.List;

import io.opentelemetry.context.propagation.TextMapSetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;

class HeadersTextMapSetter implements TextMapSetter<Message> {

    @Override
    public void set(Message message, final String key, final String value) {
        if (message != null) {
            final var headers = message.getMetadata(Headers.class)
                    .orElseThrow(() -> new IllegalStateException("Headers not found"));
            headers.put(key, List.of(value));
        }
    }
}
