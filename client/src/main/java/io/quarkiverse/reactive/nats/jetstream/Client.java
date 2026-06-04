package io.quarkiverse.reactive.nats.jetstream;

import io.quarkiverse.reactive.nats.jetstream.message.Headers;
import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

public interface Client {

    Uni<Message> publish(@NonNull Message message, @NonNull String stream, @NonNull String subject);

    Multi<Message> publish(Multi<Message> messages, String stream, String subject);

}
