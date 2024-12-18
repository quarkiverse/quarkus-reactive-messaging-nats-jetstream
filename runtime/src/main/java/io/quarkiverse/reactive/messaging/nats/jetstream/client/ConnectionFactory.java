package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;

public interface ConnectionFactory {

    <T> Uni<Connection<T>> create(ConnectionConfiguration connectionConfiguration);

    <T> Uni<Connection<T>> create(ConnectionConfiguration connectionConfiguration, ConnectionListener connectionListener);

    <T> Uni<Connection<T>> create(ConnectionConfiguration connectionConfiguration,
            List<ConnectionListener> connectionListeners);
}
