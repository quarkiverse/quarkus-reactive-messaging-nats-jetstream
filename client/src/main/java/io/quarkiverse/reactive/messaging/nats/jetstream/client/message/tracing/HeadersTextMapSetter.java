package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.opentelemetry.context.propagation.TextMapSetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.PublishHeaders;

class HeadersTextMapSetter implements TextMapSetter<Message<byte[]>> {

    @Override
    public void set(Message<byte[]> message, final @NonNull String key, final @NonNull String value) {
        if (message != null) {
            final var headers = message.getMetadata(PublishHeaders.class)
                    .orElseThrow(() -> new IllegalStateException("Headers not found"));
            headers.put(key, List.of(value));
        }
    }
}
