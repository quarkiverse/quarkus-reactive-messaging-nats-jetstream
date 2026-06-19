package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.smallrye.mutiny.Uni;

public interface ClientFactory {

    Uni<Client> create(ClientConfiguration configuration);

}
