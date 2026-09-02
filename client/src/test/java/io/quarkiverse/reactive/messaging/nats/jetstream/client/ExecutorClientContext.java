package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecutorClientContext implements ClientContext {
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
    public @NonNull CompletionStage<Void> runOnContext(@NonNull Message message, @NonNull Supplier<Void> action) {
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
    }
}
