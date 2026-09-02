package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageContext;

/**
 * Represents a client-specific context for managing tasks and ensuring execution
 * within the appropriate reactive context. This interface extends the capabilities
 * of {@link MessageContext} to provide additional functionalities related to
 * asynchronous task execution and context-aware action scheduling.
 */
public interface ClientContext extends MessageContext {

    /**
     * Provides an instance of {@link ExecutorService} to execute tasks asynchronously.
     * The returned {@link ExecutorService} facilitates task submission and
     * execution in a concurrent and non-blocking manner.
     *
     * @return a non-null instance of {@link ExecutorService} for managing task execution
     */
    @NonNull
    ExecutorService executorService();

    /**
     * Schedules the provided action to be executed on the current context. This method ensures
     * that the execution of the action occurs within the thread or execution context
     * associated with the caller or the framework.
     *
     * @param action the {@link Runnable} task to be executed, must not be null
     */
    void runOnContext(@NonNull Runnable action);

}
