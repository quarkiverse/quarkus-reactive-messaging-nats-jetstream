package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.smallrye.mutiny.Uni;

public interface ClientFactory {

    Uni<Client> create(ClientConfiguration configuration);

}
