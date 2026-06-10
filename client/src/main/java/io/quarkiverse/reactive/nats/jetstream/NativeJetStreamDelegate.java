package io.quarkiverse.reactive.nats.jetstream;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import io.nats.client.*;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.Headers;

record NativeJetStreamDelegate(io.nats.client.JetStream delegate) implements NativeJetStream {

    @Override
    public PublishAck publish(String subject, byte[] body) throws IOException, JetStreamApiException {
        return delegate.publish(subject, body);
    }

    @Override
    public PublishAck publish(String subject, Headers headers, byte[] body) throws IOException, JetStreamApiException {
        return delegate.publish(subject, headers, body);
    }

    @Override
    public PublishAck publish(String subject, byte[] body, PublishOptions options) throws IOException, JetStreamApiException {
        return delegate.publish(subject, body, options);
    }

    @Override
    public PublishAck publish(String subject, Headers headers, byte[] body, PublishOptions options)
            throws IOException, JetStreamApiException {
        return delegate.publish(subject, headers, body, options);
    }

    @Override
    public PublishAck publish(Message message) throws IOException, JetStreamApiException {
        return delegate.publish(message);
    }

    @Override
    public PublishAck publish(Message message, PublishOptions options) throws IOException, JetStreamApiException {
        return delegate.publish(message, options);
    }

    @Override
    public CompletableFuture<PublishAck> publishAsync(String subject, byte[] body) {
        return delegate.publishAsync(subject, body);
    }

    @Override
    public CompletableFuture<PublishAck> publishAsync(String subject, Headers headers, byte[] body) {
        return delegate.publishAsync(subject, headers, body);
    }

    @Override
    public CompletableFuture<PublishAck> publishAsync(String subject, byte[] body, PublishOptions options) {
        return delegate.publishAsync(subject, body, options);
    }

    @Override
    public CompletableFuture<PublishAck> publishAsync(String subject, Headers headers, byte[] body, PublishOptions options) {
        return delegate.publishAsync(subject, headers, body, options);
    }

    @Override
    public CompletableFuture<PublishAck> publishAsync(Message message) {
        return delegate.publishAsync(message);
    }

    @Override
    public CompletableFuture<PublishAck> publishAsync(Message message, PublishOptions options) {
        return delegate.publishAsync(message, options);
    }

    @Override
    public JetStreamSubscription subscribe(String subscribeSubject) throws IOException, JetStreamApiException {
        return delegate.subscribe(subscribeSubject);
    }

    @Override
    public JetStreamSubscription subscribe(String subscribeSubject, PushSubscribeOptions options)
            throws IOException, JetStreamApiException {
        return delegate.subscribe(subscribeSubject, options);
    }

    @Override
    public JetStreamSubscription subscribe(String subscribeSubject, String queue, PushSubscribeOptions options)
            throws IOException, JetStreamApiException {
        return delegate.subscribe(subscribeSubject, queue, options);
    }

    @Override
    public JetStreamSubscription subscribe(String subscribeSubject, Dispatcher dispatcher, MessageHandler handler,
            boolean autoAck) throws IOException, JetStreamApiException {
        return delegate.subscribe(subscribeSubject, dispatcher, handler, autoAck);
    }

    @Override
    public JetStreamSubscription subscribe(String subscribeSubject, Dispatcher dispatcher, MessageHandler handler,
            boolean autoAck, PushSubscribeOptions options) throws IOException, JetStreamApiException {
        return delegate.subscribe(subscribeSubject, dispatcher, handler, autoAck, options);
    }

    @Override
    public JetStreamSubscription subscribe(String subscribeSubject, String queue, Dispatcher dispatcher, MessageHandler handler,
            boolean autoAck, PushSubscribeOptions options) throws IOException, JetStreamApiException {
        return delegate.subscribe(subscribeSubject, queue, dispatcher, handler, autoAck, options);
    }

    @Override
    public JetStreamSubscription subscribe(String subscribeSubject, PullSubscribeOptions options)
            throws IOException, JetStreamApiException {
        return delegate.subscribe(subscribeSubject, options);
    }

    @Override
    public JetStreamSubscription subscribe(String subscribeSubject, Dispatcher dispatcher, MessageHandler handler,
            PullSubscribeOptions options) throws IOException, JetStreamApiException {
        return delegate.subscribe(subscribeSubject, dispatcher, handler, options);
    }

    @Override
    public StreamContext getStreamContext(String streamName) throws IOException, JetStreamApiException {
        return delegate.getStreamContext(streamName);
    }

    @Override
    public ConsumerContext getConsumerContext(String streamName, String consumerName)
            throws IOException, JetStreamApiException {
        return delegate.getConsumerContext(streamName, consumerName);
    }
}
