package io.quarkiverse.reactive.nats.consumer;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

import io.quarkiverse.reactive.nats.jetstream.Context;
import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkiverse.reactive.nats.jetstream.message.NativeMessage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

record SubscriptionDelegate(ImperativeSubscription delegate, Context context,
        ExecutorService executor) implements Subscription {

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
        return Dispatcher.of(ImperativeDispatcher.of(delegate.getDispatcher()), context, executor);
    }

    @Override
    public Uni<Message> nextMessage(Duration timeout) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.nextMessage(timeout)))
                .onItem().ifNotNull().transform(message -> Message.of(NativeMessage.of(message), context))
                .runSubscriptionOn(executor)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Message> nextMessage(long timeoutMillis) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.nextMessage(timeoutMillis)))
                .onItem().ifNotNull().transform(message -> Message.of(NativeMessage.of(message), context))
                .runSubscriptionOn(executor)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Void> unsubscribe() {
        return Uni.createFrom().<Void> item(Unchecked.supplier(() -> {
            delegate.unsubscribe();
            return null;
        }))
                .runSubscriptionOn(executor)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Subscription> unsubscribe(int after) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.unsubscribe(after)))
                .onItem().ifNotNull()
                .transform(subscription -> Subscription.of(ImperativeSubscription.of(subscription), context, executor))
                .runSubscriptionOn(executor)
                .emitOn(context::runOnContext);
    }

    @Override
    public void setPendingLimits(long maxMessages, long maxBytes) {
        delegate.setPendingLimits(maxMessages, maxBytes);
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
                .emitOn(context::runOnContext);
    }

    @Override
    public ImperativeSubscription delegate() {
        return delegate;
    }
}
