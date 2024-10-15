package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Multi;

public interface Subscription<T> extends ConnectionListener {

    Multi<Message<T>> subscribe();

}
