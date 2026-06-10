package io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Uni;

public interface TraceSupplier<T> {

    @NonNull
    Uni<Message<T>> get(@NonNull Message<T> message);

}
