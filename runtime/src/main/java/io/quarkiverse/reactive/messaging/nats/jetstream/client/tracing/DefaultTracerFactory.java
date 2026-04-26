package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultTracerFactory implements TracerFactory {

    @Override
    public <T> Tracer<T> create(TracerType tracerType) {
        return new DefaultTracer<>();
    }

}
