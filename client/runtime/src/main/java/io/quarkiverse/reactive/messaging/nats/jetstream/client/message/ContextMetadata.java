package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.smallrye.common.vertx.VertxContext;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * The Vert.x context a message is processed on. A received message is given its own duplicated context, so state
 * attached to it while receiving the message (such as the tracing span) is visible while processing it; the
 * acknowledgement of a message completes on the context of the acknowledgement metadata.
 *
 * @param context the Vert.x context; must not be null
 */
public record ContextMetadata(@NonNull Context context) {

    /**
     * @return the metadata of a new duplicated context of the current Vert.x context, if the caller runs on one
     */
    public static @NonNull Optional<ContextMetadata> capture() {
        return Optional.ofNullable(Vertx.currentContext())
                .map(context -> new ContextMetadata(VertxContext.createNewDuplicatedContext(context)));
    }

    /**
     * @param metadata the metadata items; must not be null
     * @return the given metadata with a {@linkplain #capture() captured context} when the caller runs on one
     */
    public static @NonNull Metadata capture(@NonNull Metadata metadata) {
        return capture().map(metadata::with).orElse(metadata);
    }
}
