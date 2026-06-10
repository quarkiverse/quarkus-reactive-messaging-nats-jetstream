package io.quarkiverse.reactive.nats.jetstream.message;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import io.nats.client.Connection;
import io.nats.client.Subscription;
import io.nats.client.impl.AckType;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import io.nats.client.support.Status;

record NativeMessageDelegate(io.nats.client.Message delegate) implements NativeMessage {

    @Override
    public String getSubject() {
        return delegate.getSubject();
    }

    @Override
    public String getReplyTo() {
        return delegate.getReplyTo();
    }

    @Override
    public boolean hasHeaders() {
        return delegate.hasHeaders();
    }

    @Override
    public Headers getHeaders() {
        return delegate.getHeaders();
    }

    @Override
    public boolean isStatusMessage() {
        return delegate.isStatusMessage();
    }

    @Override
    public Status getStatus() {
        return delegate.getStatus();
    }

    @Override
    public byte[] getData() {
        return delegate.getData();
    }

    @Override
    public boolean isUtf8mode() {
        return delegate.isUtf8mode();
    }

    @Override
    public Subscription getSubscription() {
        return delegate.getSubscription();
    }

    @Override
    public String getSID() {
        return delegate.getSID();
    }

    @Override
    public Connection getConnection() {
        return delegate.getConnection();
    }

    @Override
    public NatsJetStreamMetaData metaData() {
        return delegate.metaData();
    }

    @Override
    public AckType lastAck() {
        return delegate.lastAck();
    }

    @Override
    public void ack() {
        delegate.ack();
    }

    @Override
    public void ackSync(Duration timeout) throws TimeoutException, InterruptedException {
        delegate.ackSync(timeout);
    }

    @Override
    public void nak() {
        delegate.nak();
    }

    @Override
    public void nakWithDelay(Duration nakDelay) {
        delegate.nakWithDelay(nakDelay);
    }

    @Override
    public void nakWithDelay(long nakDelayMillis) {
        delegate.nakWithDelay(nakDelayMillis);
    }

    @Override
    public void term() {
        delegate.term();
    }

    @Override
    public void inProgress() {
        delegate.inProgress();
    }

    @Override
    public boolean isJetStream() {
        return delegate.isJetStream();
    }
}
