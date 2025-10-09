package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.support.Status;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class NativeErrorListener implements ErrorListener {
    
    @Override
    public void errorOccurred(io.nats.client.Connection conn, String error) {
        log.errorf("Error occurred: %s", error);
    }

    @Override
    public void exceptionOccurred(io.nats.client.Connection conn, Exception exp) {
        log.debugf("Caught exception connecting to %s with message: %s", conn.getServers(), exp.getMessage());
    }

    @Override
    public void slowConsumerDetected(io.nats.client.Connection conn, Consumer consumer) {
        log.warn("Slow consumer detected");
    }

    @Override
    public void messageDiscarded(io.nats.client.Connection conn, Message msg) {
        log.debugf("Message with id = %s discarded", msg.getSID());
    }

    @Override
    public void unhandledStatus(io.nats.client.Connection conn, JetStreamSubscription sub, Status status) {
        log.debugf("Unhandled status: %s", status);
    }

    @Override
    public void pullStatusWarning(io.nats.client.Connection conn, JetStreamSubscription sub, Status status) {
        log.debugf("Pull status warning with status: %s", status);
    }

    @Override
    public void pullStatusError(io.nats.client.Connection conn, JetStreamSubscription sub, Status status) {
        log.debugf("Pull status error with status: %s", status);
    }
}
