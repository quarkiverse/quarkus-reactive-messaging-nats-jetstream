package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

public interface TracerFactory {

    Tracer create(TracerType tracerType);

}
