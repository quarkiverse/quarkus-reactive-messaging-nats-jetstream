package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConfiguration;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class DefaultTracerFactory implements TracerFactory {
    private final JetStreamConfiguration configuration;
    private final Instance<OpenTelemetry> openTelemetryInstance;

    public <T> Tracer<T> create(TracerType tracerType) {
        return switch (tracerType) {
            case Subscribe -> new SubscribeTracer<>(configuration, openTelemetryInstance);
            case Publish -> new PublishTracer<>(configuration, openTelemetryInstance);
        };
    }
}
