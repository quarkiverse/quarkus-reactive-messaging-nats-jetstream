package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;

@ApplicationScoped
public class TracerFactory {
    private final JetStreamBuildConfiguration configuration;
    private final PayloadMapper payloadMapper;
    private final Instance<OpenTelemetry> openTelemetry;

    public TracerFactory(Instance<OpenTelemetry> openTelemetryInstance,
            JetStreamBuildConfiguration configuration,
            PayloadMapper payloadMapper) {
        this.configuration = configuration;
        this.payloadMapper = payloadMapper;
        this.openTelemetry = openTelemetryInstance;
    }

    public <T> Tracer<T> create() {
        return new DefaultTracer<>(openTelemetry, configuration, payloadMapper, true);
    }

    public <T> Tracer<T> create(boolean connector) {
        return new DefaultTracer<>(openTelemetry, configuration, payloadMapper, connector);
    }
}
