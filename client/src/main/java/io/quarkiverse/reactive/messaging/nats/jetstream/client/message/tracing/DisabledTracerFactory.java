package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Uni;

public class DisabledTracerFactory implements TracerFactory {

    @Override
    public @NonNull Tracer create(Operation operation) {
        return message -> Uni.createFrom().item(message);
    }
}
