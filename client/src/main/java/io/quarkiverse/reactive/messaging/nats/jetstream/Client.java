package io.quarkiverse.reactive.messaging.nats.jetstream;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface Client<T> extends AutoCloseable {

    @NonNull
    Uni<Message<T>> publish(@NonNull Message<T> message, @NonNull String stream, @NonNull String subject);

    @NonNull
    Multi<Message<T>> publish(@NonNull Multi<Message<T>> messages, @NonNull String stream, @NonNull String subject);

}
