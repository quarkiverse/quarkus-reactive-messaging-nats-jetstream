package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageHeaders;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.PublishHeaders;

record MessageHeadersTextMapGetter(@NonNull Operation operation) implements TextMapGetter<Message> {

    @Override
    public Iterable<String> keys(Message message) {
        if (message != null) {
            return getHeaders(message)
                    .map(Map::keySet).orElseGet(Collections::emptySet);
        }
        return Collections.emptyList();
    }

    @Override
    public String get(Message message, String key) {
        if (message != null) {
            return getHeaders(message)
                    .flatMap(headers -> Optional.ofNullable(headers.get(key)))
                    .map(values -> String.join(",", values))
                    .orElse(null);
        }
        return null;
    }

    private Optional<Headers> getHeaders(Message message) {
        return switch (operation) {
            case PUBLISH, PUBLISH_ACKNOWLEDGED -> message.getMetadata(PublishHeaders.class);
            case RECEIVE -> message.getMetadata(MessageHeaders.class);
        };
    }
}
