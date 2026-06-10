package io.quarkiverse.reactive.nats.jetstream.tracing;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.smallrye.mutiny.Uni;

public interface TraceSupplier {

    @NonNull
    Uni<Message> get(@NonNull Message message);

}
