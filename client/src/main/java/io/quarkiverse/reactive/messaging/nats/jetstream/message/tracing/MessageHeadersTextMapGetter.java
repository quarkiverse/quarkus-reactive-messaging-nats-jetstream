package io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Headers;
import org.eclipse.microprofile.reactive.messaging.Message;

class MessageHeadersTextMapGetter<T> implements TextMapGetter<Message<T>> {

    @Override
    public Iterable<String> keys(Message<T> message) {
        if (message != null) {
            return message.getMetadata(Headers.class)
                    .map(Map::keySet).orElseGet(Collections::emptySet);
        }
        return Collections.emptyList();
    }

    @Override
    public String get(Message<T> message, String key) {
        if (message != null) {
            return message.getMetadata(Headers.class)
                    .flatMap(headers -> Optional.ofNullable(headers.get(key)))
                    .map(values -> String.join(",", values))
                    .orElse(null);
        }
        return null;
    }
}
