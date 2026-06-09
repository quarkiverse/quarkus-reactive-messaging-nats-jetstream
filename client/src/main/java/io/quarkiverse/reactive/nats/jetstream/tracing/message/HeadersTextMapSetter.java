package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import io.opentelemetry.context.propagation.TextMapSetter;
import io.quarkiverse.reactive.nats.jetstream.message.Headers;
import io.quarkiverse.reactive.nats.jetstream.message.Message;

import java.util.List;

public class HeadersTextMapSetter implements TextMapSetter<Message> {

    @Override
    public void set(Message message, final String key, final String value) {
        if (message != null) {
            final var headers = message.getMetadata(Headers.class).orElseGet(Headers::of);
            headers.put(key, List.of(value));
            message.injectMetadata(headers);
        }
    }
}
