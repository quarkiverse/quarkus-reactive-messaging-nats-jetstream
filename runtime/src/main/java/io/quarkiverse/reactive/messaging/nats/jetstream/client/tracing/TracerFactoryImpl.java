package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

@ApplicationScoped
public class TracerFactoryImpl implements TracerFactory {
    private final ConnectorConfiguration configuration;
    private final Instance<OpenTelemetry> openTelemetryInstance;

    public TracerFactoryImpl(ConnectorConfiguration configuration,
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
