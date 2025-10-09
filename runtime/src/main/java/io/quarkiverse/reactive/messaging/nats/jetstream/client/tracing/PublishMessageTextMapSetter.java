package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import java.util.List;
import java.util.Optional;

import io.opentelemetry.context.propagation.TextMapSetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;

public class PublishMessageTextMapSetter implements TextMapSetter<PublishMessageMetadata> {

    @Override
    public void set(PublishMessageMetadata carrier, final String key, final String value) {
        if (carrier != null) {
            Optional.ofNullable(carrier.payload()).flatMap(payload -> Optional.ofNullable(payload.headers()))
                    .ifPresent(headers -> headers.put(key, List.of(value)));
        }
    }
}
