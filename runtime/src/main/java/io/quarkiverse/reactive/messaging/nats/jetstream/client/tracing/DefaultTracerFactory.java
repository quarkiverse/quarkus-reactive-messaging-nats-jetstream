package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import io.quarkiverse.reactive.messaging.nats.jetstream.NatsConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class DefaultTracerFactory implements TracerFactory {
    private final NatsConfiguration configuration;
    private final Instance<OpenTelemetry> openTelemetryInstance;

    public <T> Tracer<T> create(TracerType tracerType) {
        final boolean enabled = configuration.jetStream().map(NatsConfiguration.JetStream::trace).orElse(false);
        return switch (tracerType) {
            case Subscribe -> new SubscribeTracer<>(enabled, openTelemetryInstance);
            case Publish -> new PublishTracer<>(enabled, openTelemetryInstance);
        };
    }
}
