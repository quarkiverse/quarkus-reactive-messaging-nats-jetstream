package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import java.util.List;

import io.opentelemetry.context.propagation.TextMapSetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;

public class PublishMessageTextMapSetter implements TextMapSetter<PublishMessageMetadata> {

    @Override
    public void set(PublishMessageMetadata carrier, final String key, final String value) {
        if (carrier != null) {
            final var headers = carrier.headers();
            if (headers != null) {
                headers.put(key, List.of(value));
            }
        }
    }
}
