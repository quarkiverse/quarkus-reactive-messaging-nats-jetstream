package io.quarkiverse.reactive.messaging.nats.jetstream.client.context;

import java.util.Optional;

import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Vertx;

public abstract class ContextAware {
    private final ExecutionHolder executionHolder;

    public ContextAware(ExecutionHolder executionHolder) {
        this.executionHolder = executionHolder;
    }

    protected <T> T withContext(ContextConsumer<T> context) {
        return context.accept(new ContextDelegate(getVertx().getOrCreateContext()));
    }

    private Vertx getVertx() {
        return Optional.ofNullable(executionHolder.vertx()).orElseThrow(() -> new ContextException("No Vertx available"));
    }
}
