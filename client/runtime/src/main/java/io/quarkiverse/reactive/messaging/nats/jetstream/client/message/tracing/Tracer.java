package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.util.function.Function;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Uni;

public interface Tracer {

    @NonNull
    Uni<Message<byte[]>> withTrace(@NonNull Message<byte[]> message);

    /**
     * Traces an operation on the message. Tracers whose span should measure the operation start it before the
     * operation and end it when the operation terminates, recording a failure as an error. By default the message is
     * traced first and then handed to the operation.
     *
     * @param message the message to trace; must not be null
     * @param operation the operation to perform on the traced message; must not be null
     * @return a {@link Uni} emitting the result of the operation
     */
    @NonNull
    default Uni<Message<byte[]>> withTrace(@NonNull Message<byte[]> message,
            @NonNull Function<Message<byte[]>, Uni<Message<byte[]>>> operation) {
        return withTrace(message).chain(operation::apply);
    }

}
