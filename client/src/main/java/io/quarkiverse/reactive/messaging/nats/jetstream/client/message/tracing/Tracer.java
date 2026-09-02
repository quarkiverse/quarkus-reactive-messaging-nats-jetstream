package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.smallrye.mutiny.Uni;

public interface Tracer {

    @NonNull
    Uni<Message> withTrace(@NonNull Message message);

}
