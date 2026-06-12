package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.MessageInfo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

public interface Client extends AutoCloseable {

    @NonNull
    Uni<Message> publish(@NonNull Message message, @NonNull String stream, @NonNull String subject);

    @NonNull
    Multi<Message> publish(@NonNull Multi<Message> messages, @NonNull String stream, @NonNull String subject);

    @NonNull
    Uni<Message> next(@NonNull String stream, @NonNull String consumer,  @NonNull Duration timeout);

    @NonNull
    Multi<Message> fetch(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout, int batchSize);

     @NonNull
    Uni<MessageInfo> messageInfo(@NonNull String stream, long sequence);

    @NonNull
    Multi<Message> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout, int batchSize);
}
