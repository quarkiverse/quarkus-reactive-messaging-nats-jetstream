package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ErrorListener;

public interface ConnectionListener extends ErrorListener {

    void onConnected(Connection connection);
}
