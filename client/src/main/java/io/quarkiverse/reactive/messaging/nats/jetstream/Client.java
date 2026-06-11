package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.time.Duration;

public interface Client extends AutoCloseable {

    @NonNull
    Uni<Message> publish(@NonNull Message message, @NonNull String stream, @NonNull String subject);

    @NonNull
    Multi<Message> publish(@NonNull Multi<Message> messages, @NonNull String stream, @NonNull String subject);

    @NonNull
    Uni<Message> next(@NonNull String stream, @NonNull String consumer,  @NonNull Duration timeout);

    /**
    @NonNull
    Multi<Message<T>> fetch(@NonNull ConsumerConfiguration configuration, @NonNull FetchConfiguration fetchConfiguration);

    @NonNull
    Uni<Message<T>> resolve(@NonNull String stream, long sequence);

    @NonNull
    Multi<Message<T>> subscribe(@NonNull ConsumerConfiguration configuration, @NonNull PullConfiguration pullConfiguration);
*/
}
