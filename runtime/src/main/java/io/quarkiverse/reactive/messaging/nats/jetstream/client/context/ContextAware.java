package io.quarkiverse.reactive.messaging.nats.jetstream.client.context;

import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Vertx;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public interface ContextAware {

    @NonNull ExecutionHolder executionHolder();

    default <T> T withContext(ContextConsumer<T> context) {
        return context.accept(getVertx().getOrCreateContext());
    }

    private Vertx getVertx() {
        return Optional.ofNullable(executionHolder().vertx()).orElseThrow(() -> new ContextException("No Vertx available"));
    }
}
