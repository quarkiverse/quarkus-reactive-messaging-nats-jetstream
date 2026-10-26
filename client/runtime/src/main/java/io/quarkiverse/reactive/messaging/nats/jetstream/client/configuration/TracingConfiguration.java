package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.OpenTelemetryTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;

/**
 * Produces the OpenTelemetry tracer factory; only registered when the OpenTelemetry tracer is present.
 */
@Dependent
public class TracingConfiguration {

    @Produces
    @ApplicationScoped
    public TracerFactory tracerFactory(Instance<OpenTelemetry> openTelemetry, ClientConfiguration configuration) {
        return new OpenTelemetryTracerFactory(openTelemetry, configuration.tracePayload());
    }

}
