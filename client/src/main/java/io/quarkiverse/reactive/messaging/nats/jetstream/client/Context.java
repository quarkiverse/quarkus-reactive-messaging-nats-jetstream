package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jspecify.annotations.NonNull;

/**
 * Represents an execution context that facilitates task execution through an
 * {@link ExecutorService} or other execution mechanisms. Implementations of this
 * interface define how tasks are scheduled and run, enabling integration with
 * various concurrency and reactive programming models.
 */
public interface Context {

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

    /**
     * Provides a function that schedules a {@link Supplier} task to execute within a specific reactive context
     * as defined by the provided metadata. The returned function ensures that the supplied action runs in
     * compliance with the context's execution policies and environment.
     *
     * @param metadata the context-specific metadata that defines execution parameters; must not be null
     * @return a non-null {@link Function} that accepts a {@link Supplier<Void>} and returns a {@link CompletionStage<Void>}
     *         representing the asynchronous execution of the supplied task
     */
    @NonNull
    Function<Supplier<Void>, CompletionStage<Void>> runOnContext(@NonNull Metadata metadata);
}
