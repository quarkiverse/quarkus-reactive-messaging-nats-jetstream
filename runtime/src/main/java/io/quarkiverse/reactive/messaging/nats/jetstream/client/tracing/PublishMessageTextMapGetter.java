package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import java.util.Collections;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessage;

public class PublishMessageTextMapGetter<T> implements TextMapGetter<PublishMessage<T>> {

    @Override
    public Iterable<String> keys(PublishMessage<T> carrier) {
        final var headers = carrier.headers();
        if (headers != null) {
            return headers.keySet();
        }
        return Collections.emptyList();
    }

    @Override
    public String get(PublishMessage<T> carrier, String key) {
        if (carrier != null) {
            final var headers = carrier.headers();
            if (headers != null) {
                final var value = headers.get(key);
                if (value != null) {
                    return String.join(",", value);
                }
            }
        }
        return null;
    }
}
