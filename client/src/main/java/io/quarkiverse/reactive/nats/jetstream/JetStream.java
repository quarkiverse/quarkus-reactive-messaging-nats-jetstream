package io.quarkiverse.reactive.nats.jetstream;

import io.nats.client.Message;
import io.nats.client.PublishOptions;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.Headers;

import java.util.concurrent.CompletableFuture;

public interface JetStream {

    Uni<CompletableFuture<PublishAck> publishAsync(String subject, byte[] body) {
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

}
