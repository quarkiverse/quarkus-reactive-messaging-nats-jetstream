package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import java.util.Collections;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.quarkiverse.reactive.nats.jetstream.message.Message;

public class MessageHeadersTextMapGetter implements TextMapGetter<Message> {

    @Override
    public Iterable<String> keys(Message message) {
        if (headers != null) {
            return headers.keySet();
        }
        return Collections.emptyList();
    }

    @Override
    public String get(Message messages, String key) {
        if (headers != null) {
            final var value = headers.get(key);
            if (value != null) {
                return String.join(",", value);
            }
        }
        return null;
    }
}
