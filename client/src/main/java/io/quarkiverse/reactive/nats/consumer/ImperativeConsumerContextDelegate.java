package io.quarkiverse.reactive.nats.consumer;

import io.nats.client.*;
import io.nats.client.ConsumeOptions;
import io.nats.client.Dispatcher;
import io.nats.client.FetchConsumeOptions;
import io.nats.client.FetchConsumer;
import io.nats.client.IterableConsumer;
import io.nats.client.MessageConsumer;
import io.nats.client.api.ConsumerInfo;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;

record ImperativeConsumerContextDelegate(io.nats.client.ConsumerContext delegate) implements ImperativeConsumerContext {

    @Override
    public @NonNull ConsumerInfo getConsumerInfo() throws IOException, JetStreamApiException {
        return delegate.getConsumerInfo();
    }

    @Override
    public @Nullable ConsumerInfo getCachedConsumerInfo() {
        return delegate.getCachedConsumerInfo();
    }

    @Override
    public @Nullable String getConsumerName() {
        return delegate.getConsumerName();
    }

    @Override
    public @Nullable Message next() throws IOException, InterruptedException, JetStreamStatusCheckedException, JetStreamApiException {
        return delegate.next();
    }

    @Override
    public @Nullable Message next(@Nullable Duration maxWait) throws IOException, InterruptedException, JetStreamStatusCheckedException, JetStreamApiException {
        return delegate.next(maxWait);
    }

    @Override
    public @Nullable Message next(long maxWaitMillis) throws IOException, InterruptedException, JetStreamStatusCheckedException, JetStreamApiException {
        return delegate.next(maxWaitMillis);
    }

    @Override
    public io.nats.client.@NonNull FetchConsumer fetchMessages(int maxMessages) throws IOException, JetStreamApiException {
        return delegate.fetchMessages(maxMessages);
    }

    @Override
    public io.nats.client.@NonNull FetchConsumer fetchBytes(int maxBytes) throws IOException, JetStreamApiException {
        return delegate.fetchBytes(maxBytes);
    }

    @Override
    public @NonNull FetchConsumer fetch(@NonNull FetchConsumeOptions fetchConsumeOptions) throws IOException, JetStreamApiException {
        return delegate.fetch(fetchConsumeOptions);
    }

    @Override
    public io.nats.client.@NonNull IterableConsumer iterate() throws IOException, JetStreamApiException {
        return delegate.iterate();
    }

    @Override
    public @NonNull IterableConsumer iterate(io.nats.client.@NonNull ConsumeOptions consumeOptions) throws IOException, JetStreamApiException {
        return delegate.iterate(consumeOptions);
    }

    @Override
    public io.nats.client.@NonNull MessageConsumer consume(@NonNull MessageHandler handler) throws IOException, JetStreamApiException {
        return delegate.consume(handler);
    }

    @Override
    public io.nats.client.@NonNull MessageConsumer consume(io.nats.client.@Nullable Dispatcher dispatcher, @NonNull MessageHandler handler) throws IOException, JetStreamApiException {
        return delegate.consume(dispatcher, handler);
    }

    @Override
    public io.nats.client.@NonNull MessageConsumer consume(io.nats.client.@NonNull ConsumeOptions consumeOptions, @NonNull MessageHandler handler) throws IOException, JetStreamApiException {
        return null;
    }

    @Override
    public @NonNull MessageConsumer consume(@NonNull ConsumeOptions consumeOptions, @Nullable Dispatcher dispatcher, @NonNull MessageHandler handler) throws IOException, JetStreamApiException {
        return delegate.consume(consumeOptions, dispatcher, handler);
    }

    @Override
    public boolean unpin(String group) throws IOException, JetStreamApiException {
        return delegate.unpin(group);
    }
}
