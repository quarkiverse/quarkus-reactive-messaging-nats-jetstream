package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.vertx.mutiny.core.Context;

@FunctionalInterface
public interface ContextSupplier<T> {

    T get(Context context);
}
