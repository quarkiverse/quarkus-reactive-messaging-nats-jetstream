package io.quarkiverse.reactive.messaging.nats.client.context;

@FunctionalInterface
public interface ContextConsumer<T> {

    T accept(Context context);

}
