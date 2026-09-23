package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Uni;

public interface Tracer {

    @NonNull
    Uni<Message<byte[]>> withTrace(@NonNull Message<byte[]> message);

}
