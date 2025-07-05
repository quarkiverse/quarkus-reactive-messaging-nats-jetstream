package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class DefaultTracerFactory implements TracerFactory {
    private final JetStreamConfiguration configuration;
    private final Instance<OpenTelemetry> openTelemetryInstance;

    public Tracer create(TracerType tracerType) {
        final boolean enabled = configuration.trace();
        return switch (tracerType) {
            case Subscribe -> new SubscribeTracer(enabled, openTelemetryInstance);
            case Publish -> new PublishTracer(enabled, openTelemetryInstance);
        };
    }
}
