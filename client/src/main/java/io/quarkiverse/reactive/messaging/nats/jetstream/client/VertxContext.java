package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jspecify.annotations.NonNull;

import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class VertxContext implements Context {
    private final io.vertx.mutiny.core.@NonNull Context context;
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
    public @NonNull Function<Supplier<Void>, CompletionStage<Void>> runOnContext(@NonNull Metadata metadata) {
        return action -> io.smallrye.reactive.messaging.providers.helpers.VertxContext.runOnContext(context.getDelegate(),
                f -> {
                    try {
                        action.get();
                        runOnMessageContext(metadata, () -> f.complete(null));
                    } catch (Exception e) {
                        runOnMessageContext(metadata, () -> f.completeExceptionally(e));
                    }
                });
    }

    @Override
    public <T> @NonNull Message<T> withProcessingContext(@NonNull Message<T> message) {
        if (message.getMetadata(LocalContextMetadata.class).isPresent()) {
            return message;
        }
        return message.addMetadata(new LocalContextMetadata(
                io.smallrye.common.vertx.VertxContext.createNewDuplicatedContext(context.getDelegate())));
    }

    private void runOnMessageContext(@NonNull Metadata metadata, @NonNull Runnable runnable) {
        Optional<LocalContextMetadata> contextMetadata = metadata.get(LocalContextMetadata.class);
        if (contextMetadata.isPresent()) {
            io.smallrye.reactive.messaging.providers.helpers.VertxContext.runOnContext(contextMetadata.get().context(),
                    runnable);
        } else {
            runnable.run();
        }
    }
}
