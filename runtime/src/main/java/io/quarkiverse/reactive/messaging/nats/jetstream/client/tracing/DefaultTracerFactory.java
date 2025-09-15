package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

@ApplicationScoped
public class DefaultTracerFactory implements TracerFactory {
    private final JetStreamConfiguration configuration;
    private final Instance<OpenTelemetry> openTelemetryInstance;

    public DefaultTracerFactory(JetStreamConfiguration configuration,
                                Instance<OpenTelemetry> openTelemetryInstance) {
        this.configuration = configuration;
        this.openTelemetryInstance = openTelemetryInstance;
    }

    public <T> Tracer<T> create(TracerType tracerType) {
        final boolean enabled = configuration.trace();
        return switch (tracerType) {
            case Subscribe -> new SubscribeTracer<>(enabled, openTelemetryInstance);
            case Publish -> new PublishTracer<>(enabled, openTelemetryInstance);
        };
    }
}
