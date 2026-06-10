package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.smallrye.mutiny.Uni;

public interface ClientFactory {

    <T> Uni<Client<T>> create(ClientConfiguration<T> configuration);

}
