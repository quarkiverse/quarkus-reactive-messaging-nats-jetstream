package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class DefaultConnectionListener implements ConnectionListener {

    @Override
    public void onEvent(ConnectionEvent event, String message) {
        log.infof("Event: %s, message: %s", event, message);
    }
}
