package io.quarkiverse.reactive.nats.consumer;

import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkiverse.reactive.nats.jetstream.message.MessageHandler;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

/**
 * @see io.nats.client.ConsumerContext
 */
public interface ConsumerContext {

    @NonNull Uni<ConsumerInfo> getConsumerInfo();

    @Nullable ConsumerInfo getCachedConsumerInfo();

    @Nullable String getConsumerName();

    @NonNull Uni<Message> next();

    @NonNull Uni<Message> next(@Nullable Duration maxWait);

    @NonNull Uni<Message> next(long maxWaitMillis);

    @NonNull Uni<FetchConsumer> fetchMessages(int maxMessages);

    @NonNull Uni<FetchConsumer> fetchBytes(int maxBytes);

    @NonNull Uni<FetchConsumer> fetch(@NonNull FetchConsumeOptions fetchConsumeOptions);

    @NonNull Uni<IterableConsumer> iterate();

    @NonNull Uni<IterableConsumer> iterate(@NonNull ConsumeOptions consumeOptions);

    @NonNull Uni<MessageConsumer> consume(@NonNull MessageHandler handler);

    @NonNull Uni<MessageConsumer> consume(@Nullable Dispatcher dispatcher, @NonNull MessageHandler handler);

    @NonNull Uni<MessageConsumer> consume(@NonNull ConsumeOptions consumeOptions, @NonNull MessageHandler handler);

    @NonNull Uni<MessageConsumer> consume(@NonNull ConsumeOptions consumeOptions, @Nullable Dispatcher dispatcher, @NonNull MessageHandler handler);

    @NonNull Uni<Boolean> unpin(String group);

}
