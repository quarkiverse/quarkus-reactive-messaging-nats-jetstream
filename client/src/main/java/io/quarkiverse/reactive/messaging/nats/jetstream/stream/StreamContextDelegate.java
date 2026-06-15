package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

import io.nats.client.ConsumerContext;
import io.nats.client.JetStreamApiException;
import io.nats.client.OrderedConsumerContext;
import io.nats.client.PurgeOptions;
import io.nats.client.api.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public record StreamContextDelegate(io.nats.client.StreamContext delegate) implements StreamContext {

    @Override
    public @NonNull String getStreamName() {
        return delegate.getStreamName();
    }

    @Override
    public  io.nats.client.api.@NonNull StreamInfo getStreamInfo() throws IOException, JetStreamApiException {
        return delegate.getStreamInfo();
    }

    @Override
    public io.nats.client.api.@NonNull StreamInfo getStreamInfo(@Nullable StreamInfoOptions options) throws IOException, JetStreamApiException {
        return delegate.getStreamInfo(options);
    }

    @Override
    public @NonNull PurgeResponse purge() throws IOException, JetStreamApiException {
        return delegate.purge();
    }

    @Override
    public @NonNull PurgeResponse purge(PurgeOptions options) throws IOException, JetStreamApiException {
        return delegate.purge(options);
    }

    @Override
    public @NonNull ConsumerContext getConsumerContext(@NonNull String consumerName) throws IOException, JetStreamApiException {
        return delegate.getConsumerContext(consumerName);
    }

    @Override
    public @NonNull ConsumerContext createOrUpdateConsumer(@NonNull ConsumerConfiguration config) throws IOException, JetStreamApiException {
        return delegate.createOrUpdateConsumer(config);
    }

    @Override
    public @NonNull OrderedConsumerContext createOrderedConsumer(@NonNull OrderedConsumerConfiguration config) throws IOException, JetStreamApiException {
        return delegate.createOrderedConsumer(config);
    }

    @Override
    public boolean deleteConsumer(@NonNull String consumerName) throws IOException, JetStreamApiException {
        return delegate.deleteConsumer(consumerName);
    }

    @Override
    public @NonNull ConsumerInfo getConsumerInfo(@NonNull String consumerName) throws IOException, JetStreamApiException {
        return delegate.getConsumerInfo(consumerName);
    }

    @Override
    public @NonNull List<String> getConsumerNames() throws IOException, JetStreamApiException {
        return List.of();
    }

    @Override
    public @NonNull List<ConsumerInfo> getConsumers() throws IOException, JetStreamApiException {
        return delegate.getConsumers();
    }

    @Override
    public @NonNull MessageInfo getMessage(long seq) throws IOException, JetStreamApiException {
        return delegate.getMessage(seq);
    }

    @Override
    public @NonNull MessageInfo getLastMessage(@NonNull String subject) throws IOException, JetStreamApiException {
        return delegate.getLastMessage(subject);
    }

    @Override
    public @NonNull MessageInfo getFirstMessage(@NonNull String subject) throws IOException, JetStreamApiException {
        return delegate.getFirstMessage(subject);
    }

    @Override
    public @NonNull MessageInfo getNextMessage(long seq, @NonNull String subject) throws IOException, JetStreamApiException {
        return delegate.getNextMessage(seq, subject);
    }

    @Override
    public boolean deleteMessage(long seq) throws IOException, JetStreamApiException {
        return delegate.deleteMessage(seq);
    }

    @Override
    public boolean deleteMessage(long seq, boolean erase) throws IOException, JetStreamApiException {
        return delegate.deleteMessage(seq, erase);
    }
}
