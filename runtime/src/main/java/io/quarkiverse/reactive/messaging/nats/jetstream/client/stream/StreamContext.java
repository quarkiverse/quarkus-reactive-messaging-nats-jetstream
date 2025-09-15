package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

public interface StreamContext {

    @NonNull Multi<String> streamNames();

    @NonNull Multi<String> subjects(@NonNull String streamName);

    @NonNull Uni<PurgeResult> purge(String streamName);

    @NonNull Uni<Long> firstSequence(@NonNull String streamName);

    /**
     * Deletes a message, overwriting the message data with garbage
     * This can be considered an expensive (time-consuming) operation, but is more secure.
     *
     * @param stream name of the stream
     * @param sequence the sequence number of the message
     * @param erase whether to erase the message (overwriting with garbage) or only mark it as erased.
     * @throws DeleteException when message is not deleted
     */
    @NonNull Uni<Void> deleteMessage(@NonNull String stream, long sequence, boolean erase);

    @NonNull Uni<StreamState> streamState(@NonNull String streamName);

    @NonNull Uni<StreamConfiguration> configuration(@NonNull String streamName);

    @NonNull Multi<PurgeResult> purgeAll();

    @NonNull Uni<Void> addSubject(@NonNull String streamName, @NonNull String subject);

    @NonNull Uni<Void> removeSubject(@NonNull String streamName, @NonNull String subject);

    /**
     * Adds streams. The map key is the name of the stream
     */
    @NonNull Uni<StreamResult> addIfAbsent(@NonNull String name, @NonNull StreamConfiguration configuration);

}
