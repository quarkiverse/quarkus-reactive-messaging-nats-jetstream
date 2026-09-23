package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jspecify.annotations.NonNull;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecutorContext implements Context {
    private final ExecutorService executorService;

    @Override
    public @NonNull ExecutorService executorService() {
        return executorService;
    }

    @Override
    public void runOnContext(@NonNull Runnable action) {
        executorService.execute(action);
    }

    @Override
    public @NonNull Function<Supplier<Void>, CompletionStage<Void>> runOnContext(@NonNull Metadata metadata) {
        return action -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            executorService.execute(() -> {
                try {
                    action.get();
                    future.complete(null);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
            return future;
        };
    }
}
