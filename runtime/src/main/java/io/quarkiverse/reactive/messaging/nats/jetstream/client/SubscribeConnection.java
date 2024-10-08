package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Multi;

public interface SubscribeConnection<T> extends Connection {

    Multi<Message<T>> subscribe();

}
