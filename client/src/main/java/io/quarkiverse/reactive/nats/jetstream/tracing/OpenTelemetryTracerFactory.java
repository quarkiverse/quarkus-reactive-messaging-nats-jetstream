package io.quarkiverse.reactive.nats.jetstream.tracing;;

import jakarta.enterprise.inject.Instance;

import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public class OpenTelemetryTracerFactory implements TracerFactory {
    private final Instance<OpenTelemetry> openTelemetryInstance;

    @Override
    public @NonNull Tracer publish() {
        return null;
    }

    @Override
    public @NonNull Tracer subscribe() {
        return new SubscribeTracer(openTelemetryInstance);
    }
}
