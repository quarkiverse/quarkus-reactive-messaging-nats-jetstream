package io.quarkiverse.reactive.nats.jetstream.tracing;

import org.jspecify.annotations.NonNull;

public interface TracerFactory {

    @NonNull Tracer publish();

    @NonNull Tracer subscribe();

}
