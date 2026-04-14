package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class OpenTelemetryTracerFactory implements TracerFactory {
    private final Instance<OpenTelemetry> openTelemetryInstance;

    public <T> Tracer<T> create(TracerType tracerType) {
        return switch (tracerType) {
            case Subscribe -> new SubscribeTracer<>(openTelemetryInstance);
            case Publish -> new PublishTracer<>(openTelemetryInstance);
        };
    }
}
