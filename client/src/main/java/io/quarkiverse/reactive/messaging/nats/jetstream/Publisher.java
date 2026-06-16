package io.quarkiverse.reactive.messaging.nats.jetstream;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface Publisher {

    @NonNull
    Uni<Message> publish(@NonNull Message message, @NonNull String stream, @NonNull String subject);

    @NonNull
    Multi<Message> publish(@NonNull Multi<Message> messages, @NonNull String stream, @NonNull String subject);

}
