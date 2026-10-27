package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.ContextMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Metadata;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class VertxContext implements Context {
    private final io.vertx.mutiny.core.@NonNull Context context;
    @NonNull
    private final ExecutorService threadPool;

    @Override
    public @NonNull ExecutorService threadPool() {
        return threadPool;
    }

    @Override
    public void runOnContext(@NonNull Runnable action) {
        context.runOnContext(action);
    }

    @Override
    public @NonNull Function<Supplier<Void>, CompletionStage<Void>> runOnContext(@NonNull Metadata metadata) {
        return action -> {
            final var future = new CompletableFuture<Void>();
            runOn(context.getDelegate(), () -> {
                try {
                    action.get();
                    runOnMessageContext(metadata, () -> future.complete(null));
                } catch (Exception e) {
                    runOnMessageContext(metadata, () -> future.completeExceptionally(e));
                }
            });
            return future;
        };
    }

    @Override
    public <T> @NonNull Message<T> withProcessingContext(@NonNull Message<T> message) {
        if (message.getMetadata(ContextMetadata.class).isPresent()) {
            return message;
        }
        return message.addMetadata(new ContextMetadata(
                io.smallrye.common.vertx.VertxContext.createNewDuplicatedContext(context.getDelegate())));
    }

    private void runOnMessageContext(@NonNull Metadata metadata, @NonNull Runnable runnable) {
        metadata.get(ContextMetadata.class)
                .ifPresentOrElse(contextMetadata -> runOn(contextMetadata.context(), runnable), runnable);
    }

    /**
     * Runs the action directly when already on the given context, and schedules it on the context otherwise.
     */
    private static void runOn(io.vertx.core.@NonNull Context context, @NonNull Runnable action) {
        if (Vertx.currentContext() == context) {
            action.run();
        } else {
            context.runOnContext(ignored -> action.run());
        }
    }
}
