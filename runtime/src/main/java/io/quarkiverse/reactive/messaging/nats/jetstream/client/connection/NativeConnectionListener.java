package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import org.jboss.logging.Logger;

import io.nats.client.Connection;

public class NativeConnectionListener implements io.nats.client.ConnectionListener {
    private final static Logger log = Logger.getLogger(NativeConnectionListener.class);

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    @Override
    public void connectionEvent(Connection conn, Events type) {
        switch (type) {
            case CONNECTED -> log.debugf("Connected to NATS server: %s", conn.getConnectedUrl());
            case CLOSED -> log.debugf("Connection closed");
            case DISCONNECTED -> log.warnf("Connection disconnected");
            case LAME_DUCK -> log.warnf("Connection in lame duck mode: %s", conn.getConnectedUrl());
            case RECONNECTED -> log.debugf("Connection reconnected: %s", conn.getConnectedUrl());
            case RESUBSCRIBED -> log.debugf("Connection resubscribed: %s", conn.getConnectedUrl());
            case DISCOVERED_SERVERS -> log.debugf("Connection discovered servers: %s", conn.getConnectedUrl());
            default -> log.debugf("Connection event: %s", type);
        }
    }

    @Override
    public void connectionEvent(Connection conn, Events type, Long time, String uriDetails) {
        switch (type) {
            case CONNECTED -> log.debugf("Connected to NATS server: %s (timestamp: %s, uri details: %s)",
                    conn.getConnectedUrl(), time, uriDetails);
            case CLOSED -> log.debugf("Connection closed (timestamp: %s, uri: %s)", time, uriDetails);
            case DISCONNECTED -> log.warnf("Connection disconnected (timestamp: %s, uri details: %s)", time, uriDetails);
            case LAME_DUCK -> log.warnf("Connection in lame duck mode (timestamp: %s, uri details: %s)", time, uriDetails);
            case RECONNECTED -> log.debugf("Connection reconnected: %s (timestamp: %s, uri details: %s)",
                    conn.getConnectedUrl(), time, uriDetails);
            case RESUBSCRIBED -> log.debugf("Connection resubscribed: %s (timestamp: %s, uri details: %s)",
                    conn.getConnectedUrl(), time, uriDetails);
            case DISCOVERED_SERVERS -> log.debugf("Connection discovered servers: %s (timestamp: %s, uri details: %s)",
                    conn.getConnectedUrl(), time, uriDetails);
            default -> log.debugf("Connection event: %s", type);
        }

        this.connectionEvent(conn, type);
    }
}
