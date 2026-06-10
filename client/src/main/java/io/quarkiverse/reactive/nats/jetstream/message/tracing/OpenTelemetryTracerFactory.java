package io.quarkiverse.reactive.nats.jetstream.message.tracing;

import io.opentelemetry.instrumentation.api.incubator.semconv.messaging.MessageOperation;
import jakarta.enterprise.inject.Instance;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenTelemetryTracerFactory implements TracerFactory {
    private final Instance<OpenTelemetry> openTelemetryInstance;

    @Override
    public @NonNull <T> Tracer<T> create(Operation operation) {
        return switch (operation) {
            case PUBLISH -> new PublishTracer<>(openTelemetryInstance);
            case RECEIVE -> new SubscribeTracer<>(openTelemetryInstance);
            case PROCESS -> null;
        };
    }
}
