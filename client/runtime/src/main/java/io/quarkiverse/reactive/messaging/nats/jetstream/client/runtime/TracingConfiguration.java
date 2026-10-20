package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.OpenTelemetryTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration;

/**
 * Produces the OpenTelemetry tracer factory; only registered when the OpenTelemetry tracer is present.
 */
@Dependent
public class TracingConfiguration {

    @Produces
    @ApplicationScoped
    public TracerFactory tracerFactory(Instance<OpenTelemetry> openTelemetry, ConnectorConfiguration configuration) {
        return new OpenTelemetryTracerFactory(openTelemetry, configuration.tracePayload());
    }

}
