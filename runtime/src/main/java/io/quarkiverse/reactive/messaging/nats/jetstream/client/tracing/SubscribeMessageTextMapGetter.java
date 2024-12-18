package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import java.util.Collections;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessageMetadata;

public class SubscribeMessageTextMapGetter implements TextMapGetter<SubscribeMessageMetadata> {

    @Override
    public Iterable<String> keys(SubscribeMessageMetadata metadata) {
        final var headers = metadata.headers();
        if (headers != null) {
            return headers.keySet();
        }
        return Collections.emptyList();
    }

    @Override
    public String get(SubscribeMessageMetadata metadata, String key) {
        if (metadata != null) {
            final var headers = metadata.headers();
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
