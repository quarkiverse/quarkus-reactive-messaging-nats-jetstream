package io.quarkiverse.reactive.nats.jetstream;

public interface ClientFactory {

    Client create(ClientConfiguration configuration);

}
