package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.smallrye.mutiny.Uni;

public interface TraceSupplier {

    @NonNull
    Uni<Message> get(@NonNull Message message);

}
