package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Uni;

public interface TraceSupplier {

    @NonNull
    Uni<Message<byte[]>> get(@NonNull Message<byte[]> message);

}
