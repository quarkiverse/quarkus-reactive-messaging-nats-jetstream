package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import jakarta.enterprise.inject.Instance;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;

public class OpenTelemetryTracerFactory implements TracerFactory {
    private final Instance<OpenTelemetry> openTelemetryInstance;
    private final boolean tracePayload;

    public OpenTelemetryTracerFactory(Instance<OpenTelemetry> openTelemetryInstance) {
        this(openTelemetryInstance, false);
    }

    /**
     * @param openTelemetryInstance the OpenTelemetry instance; {@link GlobalOpenTelemetry} is used when it is not resolvable
     * @param tracePayload whether the message payload is recorded as the {@code messaging.nats.message.payload} span
     *        attribute
     */
    public OpenTelemetryTracerFactory(Instance<OpenTelemetry> openTelemetryInstance, boolean tracePayload) {
        this.openTelemetryInstance = openTelemetryInstance;
        this.tracePayload = tracePayload;
    }

    @Override
    public @NonNull Tracer create(@NonNull Operation operation) {
        final var openTelemetry = openTelemetry();
        return switch (operation) {
            case PUBLISH -> new PublishTracer(openTelemetry, tracePayload);
            case RECEIVE -> new ReceiveTracer(openTelemetry, tracePayload);
            case PROCESS -> new ProcessTracer(openTelemetry, tracePayload);
        };
    }

    private OpenTelemetry openTelemetry() {
        if (openTelemetryInstance.isResolvable()) {
            return openTelemetryInstance.get();
        }
        return GlobalOpenTelemetry.get();
    }
}
