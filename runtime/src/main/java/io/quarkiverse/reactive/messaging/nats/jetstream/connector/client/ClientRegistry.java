package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;

public interface ClientRegistry {
    String DEFAULT_CLIENT_NAME = "default";

    @NonNull
    Client lookup(@NonNull String datasource);

}
