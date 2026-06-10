package io.quarkiverse.reactive.nats.jetstream;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface Client {

    @NonNull
    Uni<Message> publish(@NonNull Message message, @NonNull String stream, @NonNull String subject);

    @NonNull
    Multi<Message> publish(@NonNull Multi<Message> messages, @NonNull String stream, @NonNull String subject);

    void addListener(@NonNull ClientListener listener);
}
