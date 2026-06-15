package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.MessageInfo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface Consumer {

    @NonNull
    Uni<Message> next(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout);

    @NonNull
    Multi<Message> fetch(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout, int batchSize);

    @NonNull
    Uni<MessageInfo> message(@NonNull String stream, long sequence);

    @NonNull
    Multi<Message> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout, int batchSize);

    @NonNull
    Uni<ConsumerInfo> addConsumerIfAbsent(@NonNull ConsumerConfiguration configuration);

    @NonNull
    Uni<ConsumerInfo> consumer(@NonNull String stream, @NonNull String consumer);

    @NonNull
    Multi<ConsumerInfo> consumers(@NonNull String stream);

    @NonNull
    Uni<Void> deleteConsumer(@NonNull String stream, @NonNull String consumer);

    @NonNull
    Uni<Void> pauseConsumer(@NonNull String stream, @NonNull String consumer, @NonNull ZonedDateTime pauseUntil);

    @NonNull
    Uni<Void> resumeConsumer(@NonNull String stream, @NonNull String consumer);
}
