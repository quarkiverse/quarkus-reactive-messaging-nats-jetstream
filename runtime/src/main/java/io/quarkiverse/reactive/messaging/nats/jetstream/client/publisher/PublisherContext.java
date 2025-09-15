package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

public interface PublisherContext {

    @NonNull <T> Uni<Message<T>> publish(@NonNull Message<T> message, @NonNull String stream, @NonNull String subject);

    @NonNull <T> Uni<Message<T>> publish(@NonNull Message<T> message, @NonNull String stream, @NonNull String subject, @NonNull PublishListener listener);

    @NonNull <T> Multi<Message<T>> publish(@NonNull Multi<Message<T>> messages, @NonNull String stream, @NonNull String subject);

    @NonNull <T> Multi<Message<T>> publish(@NonNull Multi<Message<T>> messages, @NonNull String stream, @NonNull String subject, @NonNull PublishListener listener);

}
