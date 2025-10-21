package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import org.jboss.logging.Logger;

import io.nats.client.Connection;

public class NativeConnectionListener implements io.nats.client.ConnectionListener {
    private final static Logger log = Logger.getLogger(NativeConnectionListener.class);

    @Override
    public void connectionEvent(Connection conn, Events type) {
        switch (type) {
            case CONNECTED -> log.debugf("Connected to NATS server: %s", conn.getConnectedUrl());
            case CLOSED -> log.debugf("Connection closed: %s", conn.getConnectedUrl());
            case DISCONNECTED -> log.warnf("Connection disconnected: %s", conn.getConnectedUrl());
            case LAME_DUCK -> log.warnf("Connection in lame duck mode: %s", conn.getConnectedUrl());
            case RECONNECTED -> log.debugf("Connection reconnected: %s", conn.getConnectedUrl());
            case RESUBSCRIBED -> log.debugf("Connection resubscribed: %s", conn.getConnectedUrl());
            case DISCOVERED_SERVERS -> log.debugf("Connection discovered servers: %s", conn.getConnectedUrl());
            default -> log.debugf("Connection event: %s", type);
        }
    }
}
