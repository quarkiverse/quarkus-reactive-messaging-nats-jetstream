package io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing;

import org.jspecify.annotations.NonNull;

public interface TracerFactory {

    @NonNull
    Tracer create(Operation operation);

}
