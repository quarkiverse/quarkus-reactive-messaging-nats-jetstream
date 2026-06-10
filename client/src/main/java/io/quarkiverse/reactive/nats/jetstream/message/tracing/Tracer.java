package io.quarkiverse.reactive.nats.jetstream.message.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Uni;

public interface Tracer<T> {

    @NonNull
    Uni<Message<T>> withTrace(@NonNull Message<T> message);

}
