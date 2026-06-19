package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageInfo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

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
    Uni<ConsumerInfo> consumer(@NonNull String stream, @NonNull String consumer);

    @NonNull
    Multi<ConsumerInfo> consumers(@NonNull String stream);
}
