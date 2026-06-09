package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.quarkiverse.reactive.nats.jetstream.message.Headers;

import java.util.Collections;

public class MessageHeadersTextMapGetter implements TextMapGetter<Headers> {

    @Override
    public Iterable<String> keys(Headers headers) {
        if (headers != null) {
            return headers.keySet();
        }
        return Collections.emptyList();
    }

    @Override
    public String get(Headers headers, String key) {
        if (headers != null) {
            final var value = headers.get(key);
            if (value != null) {
                return String.join(",", value);
            }
        }
        return null;
    }
}
