package io.quarkiverse.reactive.nats.consumer.imperative;

import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Subscription;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public record ImperativeSubscriptionDelegate(io.nats.client.Subscription delegate) implements ImperativeSubscription {

    @Override
    public String getSubject() {
        return delegate.getSubject();
    }

    @Override
    public String getQueueName() {
        return delegate.getQueueName();
    }

    @Override
    public Dispatcher getDispatcher() {
        return delegate.getDispatcher();
    }

    @Override
    public Message nextMessage(Duration timeout) throws InterruptedException, IllegalStateException {
        return delegate.nextMessage(timeout);
    }

    @Override
    public Message nextMessage(long timeoutMillis) throws InterruptedException, IllegalStateException {
        return delegate.nextMessage(timeoutMillis);
    }

    @Override
    public void unsubscribe() {
        delegate.unsubscribe();
    }

    @Override
    public Subscription unsubscribe(int after) {
        return delegate.unsubscribe(after);
    }

    @Override
    public void setPendingLimits(long maxMessages, long maxBytes) {
        delegate.setPendingLimits(maxMessages, maxBytes);
    }

    @Override
    public long getPendingMessageLimit() {
        return delegate.getPendingMessageLimit();
    }

    @Override
    public long getPendingByteLimit() {
        return delegate.getPendingByteLimit();
    }

    @Override
    public long getPendingMessageCount() {
        return delegate.getPendingMessageCount();
    }

    @Override
    public long getPendingByteCount() {
        return delegate.getPendingByteCount();
    }

    @Override
    public long getDeliveredCount() {
        return delegate.getDeliveredCount();
    }

    @Override
    public long getDroppedCount() {
        return delegate.getDroppedCount();
    }

    @Override
    public void clearDroppedCount() {
        delegate.clearDroppedCount();
    }

    @Override
    public boolean isActive() {
        return delegate.isActive();
    }

    @Override
    public CompletableFuture<Boolean> drain(Duration timeout) throws InterruptedException {
        return delegate.drain(timeout);
    }
}
