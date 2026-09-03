package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.OpenTelemetryTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;

@Dependent
public class TracingConfiguration {

    @Produces
    public TracerFactory tracerFactory(Instance<OpenTelemetry> openTelemetry) {
        return new OpenTelemetryTracerFactory(openTelemetry);
    }

}
