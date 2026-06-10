package io.quarkiverse.reactive.nats.jetstream;

import io.smallrye.mutiny.Uni;

public interface ClientFactory {

    <T> Uni<Client<T>> create(ClientConfiguration<T> configuration);

}
