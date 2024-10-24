package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;

public interface ConnectionFactory {

    Uni<? extends Connection> create(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener);
}
