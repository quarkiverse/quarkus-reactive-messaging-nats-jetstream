package io.quarkiverse.reactive.nats.consumer;

import io.quarkiverse.reactive.nats.Context;
import io.quarkiverse.reactive.nats.message.Message;
import io.quarkiverse.reactive.nats.message.MessageHandler;
import io.quarkiverse.reactive.nats.message.imperative.ImperativeMessage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

record DispatcherDelegate(ImperativeDispatcher delegate, Context context, ExecutorService executor) implements Dispatcher {

    @Override
    public Uni<Void> start(String id) {
        return Uni.createFrom().<Void>item(Unchecked.supplier(() -> {
                    delegate.start(id);
                    return null;
                }))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Dispatcher> subscribe(String subject) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.subscribe(subject)))
                .onItem().ifNotNull().transform(dispatcher -> Dispatcher.of(ImperativeDispatcher.of(dispatcher), context, executor))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Dispatcher> subscribe(String subject, String queue) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.subscribe(subject, queue)))
                .onItem().ifNotNull().transform(dispatcher -> Dispatcher.of(ImperativeDispatcher.of(dispatcher), context, executor))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Subscription> subscribe(String subject, MessageHandler handler) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.subscribe(subject, msg -> handler.onMessage(Message.of(ImperativeMessage.of(msg), context)))))
                .onItem().ifNotNull().transform(subscription -> Subscription.of(ImperativeSubscription.of(subscription), context, executor))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Subscription> subscribe(String subject, String queue, MessageHandler handler) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.subscribe(subject, queue, msg -> handler.onMessage(Message.of(ImperativeMessage.of(msg), context)))))
                .onItem().ifNotNull().transform(subscription -> Subscription.of(ImperativeSubscription.of(subscription), context, executor))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Dispatcher> unsubscribe(String subject) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.unsubscribe(subject)))
                .onItem().ifNotNull().transform(dispatcher -> Dispatcher.of(ImperativeDispatcher.of(dispatcher), context, executor))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Dispatcher> unsubscribe(Subscription subscription) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.unsubscribe(subscription.delegate())))
                .onItem().ifNotNull().transform(dispatcher -> Dispatcher.of(ImperativeDispatcher.of(dispatcher), context, executor))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Dispatcher> unsubscribe(String subject, int after) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.unsubscribe(subject, after)))
                .onItem().ifNotNull().transform(dispatcher -> Dispatcher.of(ImperativeDispatcher.of(dispatcher), context, executor))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Dispatcher> unsubscribe(Subscription subscription, int after) {
        return Uni.createFrom().item(Unchecked.supplier(() -> delegate.unsubscribe(subscription.delegate(), after)))
                .onItem().ifNotNull().transform(dispatcher -> Dispatcher.of(ImperativeDispatcher.of(dispatcher), context, executor))
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
        return null;
    }
}
