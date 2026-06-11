package io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing;

import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Uni;

public interface TraceSupplier {

    @NonNull
    Uni<Message> get(@NonNull Message message);

}
