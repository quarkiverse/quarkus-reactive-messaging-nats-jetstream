package io.quarkiverse.reactive.messaging.nats.client.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public record ContextDelegate(io.vertx.mutiny.core.Context delegate) implements Context {

    @Override
    public io.vertx.core.Context getDelegate() {
        return delegate.getDelegate();
    }

    @Override
    public <T> Uni<T> execute(Uni<T> codeHandler) {
        ExecutorService executor = Executors.newSingleThreadExecutor(ContextWorkerThread::new);
        return codeHandler
                .runSubscriptionOn(executor)
                .emitOn(delegate::runOnContext);
    }

    @Override
    public <T> Multi<T> execute(Multi<T> codeHandler) {
        ExecutorService executor = Executors.newSingleThreadExecutor(ContextWorkerThread::new);
        return codeHandler
                .runSubscriptionOn(executor)
                .emitOn(delegate::runOnContext);
    }

    @Override
    public void runOnContext(Runnable action) {
        delegate.runOnContext(action);
    }
}
