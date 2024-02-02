package io.quarkiverse.reactive.messaging.nats.jetstream.tracing;

import java.util.List;

import io.opentelemetry.context.propagation.TextMapSetter;

public enum JetStreamTraceTextMapSetter implements TextMapSetter<JetStreamTrace> {
    INSTANCE;

    @Override
    public void set(final JetStreamTrace carrier, final String key, final String value) {
        if (carrier != null) {
            final var headers = carrier.headers();
            if (headers != null) {
                headers.put(key, List.of(value));
            }
        }
    }
}
