package io.quarkiverse.reactive.messaging.nats.jetstream.client.context;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Vertx;

public abstract class ContextAware {
    private final ExecutionHolder executionHolder;
    private final ExecutorService executorService;

    public ContextAware(ExecutionHolder executionHolder, ExecutorService executorService) {
        this.executionHolder = executionHolder;
        this.executorService = executorService;
    }

    protected <T> T withContext(ContextConsumer<T> context) {
        return context.accept(new ContextDelegate(getVertx().getOrCreateContext(), executorService));
    }

    private Vertx getVertx() {
        return Optional.ofNullable(executionHolder.vertx()).orElseThrow(() -> new ContextException("No Vertx available"));
    }
}
