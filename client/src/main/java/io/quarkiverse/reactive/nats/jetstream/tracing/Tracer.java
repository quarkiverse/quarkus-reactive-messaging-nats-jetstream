package io.quarkiverse.reactive.nats.jetstream.tracing;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.smallrye.mutiny.Uni;

public interface Tracer {

    @NonNull
    Uni<Message> withTrace(@NonNull Message message);

}
