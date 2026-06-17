package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.ZonedDateTime;

import io.quarkiverse.reactive.messaging.nats.jetstream.stream.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.stream.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.stream.StreamInfo;
import io.smallrye.mutiny.Multi;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerInfo;
import io.smallrye.mutiny.Uni;

public interface StreamManagement {

    @NonNull
    Uni<ConsumerInfo> addConsumerIfAbsent(@NonNull String stream, @NonNull ConsumerConfiguration configuration);

    @NonNull
    Uni<Void> deleteConsumer(@NonNull String stream, @NonNull String consumer);

    @NonNull
    Uni<Void> pauseConsumer(@NonNull String stream, @NonNull String consumer, @NonNull ZonedDateTime pauseUntil);

    @NonNull
    Uni<Void> resumeConsumer(@NonNull String stream, @NonNull String consumer);

    @NonNull
    Uni<PurgeResult> purge(@NonNull String stream);

    /**
     * Deletes a message, overwriting the message data with garbage
     * This can be considered an expensive (time-consuming) operation, but is more secure.
     *
     * @param stream name of the stream
     * @param sequence the sequence number of the message
     * @param erase whether to erase the message (overwriting with garbage) or only mark it as erased.
     */
    @NonNull
    Uni<Void> deleteMessage(@NonNull String stream, long sequence, boolean erase);

    @NonNull
    Multi<PurgeResult> purgeAll();

    @NonNull
    Uni<StreamInfo> addSubject(@NonNull String stream, @NonNull String subject);

    @NonNull
    Uni<Void> removeSubject(@NonNull String stream, @NonNull String subject);

    /**
     * Adds streams. The map key is the name of the stream
     */
    @NonNull
    Uni<StreamInfo> addStreamIfAbsent(@NonNull StreamConfiguration configuration);

}
