package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

public interface TracerFactory {

    <T> Tracer<T> create(TracerType tracerType);

}
