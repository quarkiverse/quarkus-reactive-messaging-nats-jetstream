package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.ZonedDateTime;

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

}
