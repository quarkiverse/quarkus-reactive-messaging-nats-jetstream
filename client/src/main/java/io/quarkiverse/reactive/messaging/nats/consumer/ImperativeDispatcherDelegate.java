package io.quarkiverse.reactive.messaging.nats.consumer;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import io.nats.client.Dispatcher;
import io.nats.client.MessageHandler;
import io.nats.client.Subscription;

record ImperativeDispatcherDelegate(io.nats.client.Dispatcher delegate) implements ImperativeDispatcher {

    @Override
    public void start(String id) {
        delegate.start(id);
    }

    @Override
    public Dispatcher subscribe(String subject) {
        return delegate.subscribe(subject);
    }

    @Override
    public Dispatcher subscribe(String subject, String queue) {
        return delegate.subscribe(subject, queue);
    }

    @Override
    public Subscription subscribe(String subject, MessageHandler handler) {
        return delegate.subscribe(subject, handler);
    }

    @Override
    public Subscription subscribe(String subject, String queue, MessageHandler handler) {
        return delegate.subscribe(subject, queue, handler);
    }

    @Override
    public Dispatcher unsubscribe(String subject) {
        return delegate.unsubscribe(subject);
    }

    @Override
    public Dispatcher unsubscribe(Subscription subscription) {
        return delegate.unsubscribe(subscription);
    }

    @Override
    public Dispatcher unsubscribe(String subject, int after) {
        return delegate.unsubscribe(subject, after);
    }

    @Override
    public Dispatcher unsubscribe(Subscription subscription, int after) {
        return delegate.unsubscribe(subscription, after);
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
