package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.time.ZonedDateTime;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface ConsumerManagement {

    /**
     * Retrieves the consumer instance for the specified stream and consumer name.
     *
     * @param consumer the name of the consumer to retrieve; must not be null
     * @return a {@link Uni} that resolves to the {@link Consumer}
     *         instance representing the specified consumer
     */
    @NonNull
    Uni<Consumer> consumer(@NonNull String consumer);

    /**
     * Retrieves a reactive stream of consumer instances associated with the specified stream.
     *
     * @return a {@link Multi} emitting {@link Consumer}
     *         instances associated with the specified stream
     */
    @NonNull
    Multi<Consumer> consumers();

    /**
     * Adds a new consumer to the specified stream if it does not already exist.
     *
     * @param configuration the configuration settings for the consumer; must not be null
     * @return a Uni emitting the created or existing consumer if found
     */
    @NonNull
    Uni<Consumer> addIfAbsent(@NonNull ConsumerConfiguration configuration);

    /**
     * Deletes the specified consumer from the given stream.
     *
     * @param consumer the name of the consumer to be deleted; must not be null.
     * @return a Uni emitting a void result once the consumer is successfully deleted or an error if the operation fails.
     */
    @NonNull
    Uni<Void> delete(@NonNull String consumer);

    /**
     * Pauses the specified consumer from the given stream.
     *
     * @param consumer the name of the consumer to be paused; must not be null.
     * @param pauseUntil the time until which the consumer will be paused; must not be null.
     * @return a Uni emitting a void result once the consumer is successfully paused or an error if the operation fails.
     */
    @NonNull
    Uni<Void> pause(@NonNull String consumer, @NonNull ZonedDateTime pauseUntil);

    /**
     * Resumes the specified consumer from the given stream.
     *
     * @param consumer the name of the consumer to be resumed; must not be null.
     * @return a Uni emitting a void result once the consumer is successfully resumed or an error if the operation fails.
     */
    @NonNull
    Uni<Void> resume(@NonNull String consumer);
}
