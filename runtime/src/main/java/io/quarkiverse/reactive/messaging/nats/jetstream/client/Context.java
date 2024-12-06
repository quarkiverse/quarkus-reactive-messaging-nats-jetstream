package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Vertx;

@ApplicationScoped
public class Context {
    private final ExecutionHolder executionHolder;

    public Context(ExecutionHolder executionHolder) {
        this.executionHolder = executionHolder;
    }

    public <T> T withContext(ContextSupplier<T> supplier) {
        final var context = getContext();
        return supplier.get(context);
    }

    private Optional<Vertx> getVertx() {
        return Optional.ofNullable(executionHolder.vertx());
    }

    private io.vertx.mutiny.core.Context getContext() {
        return getVertx().map(Vertx::getOrCreateContext).orElseThrow(() -> new ContextException("No Vertx available"));
    }
}
