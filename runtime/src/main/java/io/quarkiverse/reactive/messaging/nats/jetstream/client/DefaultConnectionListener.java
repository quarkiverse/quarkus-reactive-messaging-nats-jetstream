package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.jboss.logging.Logger;

public class DefaultConnectionListener implements ConnectionListener {
    private final static Logger logger = Logger.getLogger(DefaultConnectionListener.class);

    @Override
    public void onEvent(ConnectionEvent event, String message) {
        logger.infof("Event: %s, message: %s", event, message);
    }

    @Override
    public void close() throws Exception {

    }
}
