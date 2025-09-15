package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.smallrye.mutiny.Uni;

@FunctionalInterface
public interface ConnectionConsumer<T> {

    Uni<T> accept(Connection connection);

}
