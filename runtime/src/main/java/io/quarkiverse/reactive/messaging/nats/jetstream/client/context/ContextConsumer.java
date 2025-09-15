package io.quarkiverse.reactive.messaging.nats.jetstream.client.context;

import io.vertx.mutiny.core.Context;

@FunctionalInterface
public interface ContextConsumer<T> {

    T accept(Context context);

}
