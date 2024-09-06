package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.jboss.logging.Logger;

import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.support.Status;

class DefaultErrorListener implements ErrorListener {
    private final static Logger logger = Logger.getLogger(DefaultErrorListener.class);

    @Override
    public void errorOccurred(io.nats.client.Connection conn, String error) {
        logger.errorf("Error occurred: %s", error);
    }

    @Override
    public void exceptionOccurred(io.nats.client.Connection conn, Exception exp) {
        logger.errorf("Caught exception connecting to %s with message: %s", conn.getServers(), exp.getMessage());
    }

    @Override
    public void slowConsumerDetected(io.nats.client.Connection conn, Consumer consumer) {
        logger.warn("Slow consumer detected");
    }

    @Override
    public void messageDiscarded(io.nats.client.Connection conn, Message msg) {
        logger.debugf("Message with id = %s discarded", msg.getSID());
    }

    @Override
    public void unhandledStatus(io.nats.client.Connection conn, JetStreamSubscription sub, Status status) {
        logger.debugf("Unhandled status: %s", status);
    }

    @Override
    public void pullStatusWarning(io.nats.client.Connection conn, JetStreamSubscription sub, Status status) {
        logger.debugf("Pull status warning with status: %s", status);
    }

    @Override
    public void pullStatusError(io.nats.client.Connection conn, JetStreamSubscription sub, Status status) {
        logger.debugf("Pull status error with status: %s", status);
    }
}
