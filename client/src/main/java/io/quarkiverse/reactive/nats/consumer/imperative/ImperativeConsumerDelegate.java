package io.quarkiverse.reactive.nats.consumer.imperative;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

record ImperativeConsumerDelegate(io.nats.client.Consumer delegate) implements ImperativeConsumer {

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
