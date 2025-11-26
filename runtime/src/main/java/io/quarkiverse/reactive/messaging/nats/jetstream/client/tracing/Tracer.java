package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.smallrye.mutiny.Uni;

public interface Tracer<T> {

    Uni<Message<T>> withTrace(Message<T> message, TraceSupplier<T> traceSupplier);

    default OpenTelemetry getOpenTelemetry(Instance<OpenTelemetry> openTelemetryInstance) {
        if (openTelemetryInstance.isResolvable()) {
            return openTelemetryInstance.get();
        }
        return GlobalOpenTelemetry.get();
    }

}
