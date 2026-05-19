package io.quarkiverse.reactive.nats.consumer;

import io.quarkiverse.reactive.nats.Context;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

public record ConsumerDelegate(NativeConsumer delegate, Context context, ExecutorService executorService) implements Consumer {

    @Override
    public void setPendingLimits(long maxMessages, long maxBytes) {
        delegate.setPendingLimits(maxMessages,maxBytes);
    }

    @Override
    public Long getPendingMessageLimit() {
        return delegate.getPendingMessageLimit();
    }

    @Override
    public Long getPendingByteLimit() {
        return delegate.getPendingByteLimit();
    }

    @Override
    public Long getPendingMessageCount() {
        return delegate.getPendingMessageCount();
    }

    @Override
    public Long getPendingByteCount() {
        return delegate.getPendingByteCount();
    }

    @Override
    public Long getDeliveredCount() {
        return delegate.getDeliveredCount();
    }

    @Override
    public Long getDroppedCount() {
        return delegate.getDroppedCount();
    }

    @Override
    public void clearDroppedCount() {
        delegate.clearDroppedCount();
    }

    @Override
    public Boolean isActive() {
        return delegate.isActive();
    }

    @Override
    public Uni<Boolean> drain(Duration timeout) {
        return Uni.createFrom().future(Unchecked.supplier(() -> delegate.drain(timeout)))
                .runSubscriptionOn(executorService)
                .emitOn(context::runOnContext);
    }
}
