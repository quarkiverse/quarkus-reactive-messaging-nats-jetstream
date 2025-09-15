package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ErrorListener;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class DefaultConnectionListener implements ConnectionListener {
    private final ErrorListener errorListener;

    public DefaultConnectionListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public void onError(Throwable throwable) {
        errorListener.onError(throwable);
    }

    @Override
    public void onConnected(Connection connection) {
        log.infof("Connected to %s", connection.getConnectedUrl());
    }
}
