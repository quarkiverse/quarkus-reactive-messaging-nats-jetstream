package io.quarkiverse.reactive.nats.jetstream.tracing;;

import jakarta.enterprise.inject.Instance;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenTelemetryTracerFactory implements TracerFactory {
    private final Instance<OpenTelemetry> openTelemetryInstance;

    @Override
    public @NonNull Tracer publish() {
        return new PublishTracer(openTelemetryInstance);
    }

    @Override
    public @NonNull Tracer subscribe() {
        return new SubscribeTracer(openTelemetryInstance);
    }
}
