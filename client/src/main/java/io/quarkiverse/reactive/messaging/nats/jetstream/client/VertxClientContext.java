package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.smallrye.reactive.messaging.providers.helpers.VertxContext;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class VertxClientContext implements ClientContext {
    @NonNull
    private final Context context;
    @NonNull
    private final ExecutorService executorService;

    @Override
    public @NonNull ExecutorService executorService() {
        return executorService;
    }

    @Override
    public void runOnContext(@NonNull Runnable action) {
        context.runOnContext(action);
    }

    @Override
    public @NonNull CompletionStage<Void> runOnContext(@NonNull Message message, @NonNull Supplier<Void> action) {
        return VertxContext.runOnContext(context.getDelegate(), f -> {
            try {
                action.get();
                runOnMessageContext(message, () -> f.complete(null));
            } catch (Exception e) {
                runOnMessageContext(message, () -> f.completeExceptionally(e));
            }
        });
    }

    private void runOnMessageContext(@NonNull Message message, @NonNull Runnable runnable) {
        Optional<LocalContextMetadata> contextMetadata = message.getContextMetadata();
        if (contextMetadata.isPresent()) {
            VertxContext.runOnContext(contextMetadata.get().context(), runnable);
        } else {
            runnable.run();
        }
    }
}
