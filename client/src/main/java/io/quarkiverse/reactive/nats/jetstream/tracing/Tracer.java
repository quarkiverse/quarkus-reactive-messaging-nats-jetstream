package io.quarkiverse.reactive.nats.jetstream.tracing;

import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

public interface Tracer {

    @NonNull Uni<Message> withTrace(@NonNull Message message);

}
