package io.quarkiverse.reactive.messaging.nats.jetstream.client.context;

import java.util.Optional;

import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Vertx;

public interface ContextAware {

    ExecutionHolder executionHolder();

    default <T> T withContext(ContextConsumer<T> context) {
        return context.accept(getVertx().getOrCreateContext());
    }

    private Vertx getVertx() {
        return Optional.ofNullable(executionHolder().vertx()).orElseThrow(() -> new ContextException("No Vertx available"));
    }
}
