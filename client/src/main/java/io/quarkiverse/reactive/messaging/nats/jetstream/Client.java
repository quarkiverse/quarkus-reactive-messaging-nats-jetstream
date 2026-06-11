package io.quarkiverse.reactive.messaging.nats.jetstream;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.time.Duration;

public interface Client<T> extends AutoCloseable {

    @NonNull
    Uni<Message<T>> publish(@NonNull Message<T> message, @NonNull String stream, @NonNull String subject);

    @NonNull
    Multi<Message<T>> publish(@NonNull Multi<Message<T>> messages, @NonNull String stream, @NonNull String subject);

    @NonNull
    Uni<Message<T>> next(@NonNull String stream, @NonNull String consumer,  @NonNull Duration timeout);

    /**
    @NonNull
    Multi<Message<T>> fetch(@NonNull ConsumerConfiguration configuration, @NonNull FetchConfiguration fetchConfiguration);

    @NonNull
    Uni<Message<T>> resolve(@NonNull String stream, long sequence);

    @NonNull
    Multi<Message<T>> subscribe(@NonNull ConsumerConfiguration configuration, @NonNull PullConfiguration pullConfiguration);
*/
}
