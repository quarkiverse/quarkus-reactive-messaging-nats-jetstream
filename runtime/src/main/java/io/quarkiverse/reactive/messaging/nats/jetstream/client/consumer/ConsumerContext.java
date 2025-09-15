package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface ConsumerContext {

    /**
     * Adds a consumer
     *
     * @param stream The name of the stream
     * @param name The name of the consumer
     * @param configuration The consumer configuration
     * @return The consumer
     */
    @NonNull Uni<Consumer> addIfAbsent(@NonNull String stream, @NonNull String name, @NonNull ConsumerConfiguration configuration);

    @NonNull Uni<Consumer> addIfAbsent(@NonNull String stream, @NonNull String name, @NonNull PushConsumerConfiguration configuration);

    @NonNull Uni<Consumer> addIfAbsent(@NonNull String stream, @NonNull String name, @NonNull PullConsumerConfiguration configuration);

    @NonNull Uni<Consumer> get(@NonNull String stream, @NonNull String consumerName);

    @NonNull Multi<String> names(@NonNull String streamName);

    @NonNull Uni<Void> delete(@NonNull String streamName, @NonNull String consumerName);

    @NonNull Uni<Void> pause(@NonNull String streamName, @NonNull String consumerName, @NonNull ZonedDateTime pauseUntil);

    @NonNull Uni<Void> resume(@NonNull String streamName, @NonNull String consumerName);

    @NonNull <T> Uni<Message<T>> next(@NonNull String stream, @NonNull String consumer, @NonNull ConsumerConfiguration configuration, @NonNull Duration timeout);

    @NonNull <T> Multi<Message<T>> fetch(@NonNull String stream, @NonNull String consumer, @NonNull FetchConsumerConfiguration configuration);

    @NonNull <T> Uni<Message<T>> resolve(@NonNull String stream, long sequence);

    @NonNull <T> Multi<Message<T>> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull PullConsumerConfiguration configuration);

    @NonNull <T> Multi<Message<T>> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull PushConsumerConfiguration configuration);

}
