package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;

/**
 * Provides a context for executing actions associated with a specific {@code Message}.
 * This interface is designed to manage the execution of actions within a message-specific
 * context to ensure proper handling and isolation. Implementations are expected to
 * perform context-sensitive tasks for the given message.
 */
public interface MessageContext {

    /**
     * Executes the given action on the context associated with the provided message.
     * This method ensures that the action is performed within the appropriate context
     * for the provided message.
     *
     * @param message the {@code Message} instance whose context is used; must not be null
     * @param action a {@code Supplier} representing the action to be executed within the message's context; must not be null
     * @return a {@code CompletionStage<Void>} that completes when the action has been executed
     */
    @NonNull
    CompletionStage<Void> runOnContext(@NonNull Message message, @NonNull Supplier<Void> action);

}
