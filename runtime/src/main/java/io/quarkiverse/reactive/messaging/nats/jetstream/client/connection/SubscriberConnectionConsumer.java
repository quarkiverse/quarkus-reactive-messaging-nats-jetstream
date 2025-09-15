package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.smallrye.mutiny.Multi;

@FunctionalInterface
public interface SubscriberConnectionConsumer<T> {

    Multi<T> accept(Connection connection);
}
