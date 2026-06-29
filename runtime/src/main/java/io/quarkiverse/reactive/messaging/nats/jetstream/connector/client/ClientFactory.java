package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

public interface ClientFactory {

    @NonNull
    Uni<Client> create(@NonNull String datasource);

    @NonNull
    Uni<Client> create();
}
