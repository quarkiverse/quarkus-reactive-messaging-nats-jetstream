package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;

public interface ConnectionFactory {

    Uni<Connection> create(ConnectionConfiguration connectionConfiguration);

    Uni<Connection> create(ConnectionConfiguration connectionConfiguration, ConnectionListener connectionListener);

    Uni<Connection> create(ConnectionConfiguration connectionConfiguration,
            List<ConnectionListener> connectionListeners);
}
