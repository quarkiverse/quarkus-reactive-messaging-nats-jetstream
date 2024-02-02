package io.quarkiverse.reactive.messaging.nats.jetstream.tracing;

import java.util.Collections;

import jakarta.annotation.Nullable;

import io.opentelemetry.context.propagation.TextMapGetter;

public enum JetStreamTraceTextMapGetter implements TextMapGetter<JetStreamTrace> {
    INSTANCE;

    @Override
    public Iterable<String> keys(JetStreamTrace carrier) {
        final var headers = carrier.headers();
        if (headers != null) {
            return headers.keySet();
        }
        return Collections.emptyList();
    }

    @Override
    public String get(@Nullable JetStreamTrace carrier, String key) {
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
