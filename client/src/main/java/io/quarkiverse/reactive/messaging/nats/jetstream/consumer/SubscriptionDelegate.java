package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.nats.client.*;
import io.nats.client.Dispatcher;
import io.nats.client.api.ConsumerInfo;

record SubscriptionDelegate(JetStreamSubscription delegate) implements Subscription {

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
    public io.nats.client.Subscription unsubscribe(int after) {
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

    @Override
    public String getConsumerName() {
        return delegate.getConsumerName();
    }

    @Override
    public void pull(int batchSize) {
        delegate.pull(batchSize);
    }

    @Override
    public void pull(PullRequestOptions pullRequestOptions) {
        delegate.pull(pullRequestOptions);
    }

    @Override
    public void pullNoWait(int batchSize) {
        delegate.pullNoWait(batchSize);
    }

    @Override
    public void pullNoWait(int batchSize, Duration expiresIn) {
        delegate.pullNoWait(batchSize, expiresIn);
    }

    @Override
    public void pullNoWait(int batchSize, long expiresInMillis) {
        delegate.pullNoWait(batchSize, expiresInMillis);
    }

    @Override
    public void pullExpiresIn(int batchSize, Duration expiresIn) {
        delegate.pullExpiresIn(batchSize, expiresIn);
    }

    @Override
    public void pullExpiresIn(int batchSize, long expiresInMillis) {
        delegate.pullExpiresIn(batchSize, expiresInMillis);
    }

    @Override
    public List<Message> fetch(int batchSize, Duration maxWait) {
        return delegate.fetch(batchSize, maxWait);
    }

    @Override
    public List<Message> fetch(int batchSize, long maxWaitMillis) {
        return delegate.fetch(batchSize, maxWaitMillis);
    }

    @Override
    public Iterator<Message> iterate(int batchSize, Duration maxWait) {
        return delegate.iterate(batchSize, maxWait);
    }

    @Override
    public Iterator<Message> iterate(int batchSize, long maxWaitMillis) {
        return delegate.iterate(batchSize, maxWaitMillis);
    }

    @Override
    public JetStreamReader reader(int batchSize, int repullAt) {
        return delegate.reader(batchSize, repullAt);
    }

    @Override
    public ConsumerInfo getConsumerInfo() throws IOException, JetStreamApiException {
        return delegate.getConsumerInfo();
    }
}
