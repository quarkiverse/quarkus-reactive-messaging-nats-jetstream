package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.threads.EnhancedQueueExecutor;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ThreadPoolConfiguration;
import lombok.extern.jbosslog.JBossLog;

/**
 * The thread pool owned by a {@link Client}, shut down when the client is closed. Backed either by an
 * {@link EnhancedQueueExecutor}, which (unlike {@link java.util.concurrent.ThreadPoolExecutor}) starts new threads up
 * to the maximum size before queueing tasks, or by an executor starting a new virtual thread per task. Virtual
 * threads need Java 21, so they are looked up by reflection, and the platform thread pool is used when the running
 * Java version does not support them.
 */
@JBossLog
final class ClientThreadPool implements AutoCloseable {
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger();

    private final ExecutorService executor;
    private final Duration shutdownTimeout;

    private ClientThreadPool(@NonNull ExecutorService executor, @NonNull Duration shutdownTimeout) {
        this.executor = executor;
        this.shutdownTimeout = shutdownTimeout;
    }

    static @NonNull ClientThreadPool of(@NonNull ThreadPoolConfiguration configuration) {
        final var prefix = "nats-client-" + POOL_NUMBER.incrementAndGet();
        if (configuration.virtualThreads()) {
            final var executor = virtualThreadPerTaskExecutor(prefix);
            if (executor.isPresent()) {
                return new ClientThreadPool(executor.get(), configuration.shutdownTimeout());
            }
        }
        final var maxSize = configuration.maxSize().orElseGet(() -> 8 * Runtime.getRuntime().availableProcessors());
        if (configuration.coreSize() < 0 || maxSize < 1 || configuration.coreSize() > maxSize) {
            throw new IllegalArgumentException(String.format(
                    "Invalid thread pool size: core size %d must be between 0 and max size %d, and max size at least 1",
                    configuration.coreSize(), maxSize));
        }
        final var builder = new EnhancedQueueExecutor.Builder()
                .setCorePoolSize(configuration.coreSize())
                .setMaximumPoolSize(maxSize)
                .setKeepAliveTime(configuration.keepAlive())
                .setThreadFactory(threadFactory(prefix))
                .setRegisterMBean(false);
        configuration.queueSize().ifPresentOrElse(
                queueSize -> builder.setQueueLimited(true).setMaximumQueueSize(queueSize),
                () -> builder.setQueueLimited(false));
        return new ClientThreadPool(builder.build(), configuration.shutdownTimeout());
    }

    @NonNull
    ExecutorService executorService() {
        return executor;
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                log.warnf("Thread pool did not terminate within %s, interrupting running tasks", shutdownTimeout);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates the equivalent of
     * {@code Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name(prefix + "-virtual-thread-", 1).factory())}
     * by reflection, or returns empty when the running Java version does not support virtual threads (before Java
     * 21, or Java 19 and 20 without preview features enabled).
     */
    private static @NonNull Optional<ExecutorService> virtualThreadPerTaskExecutor(@NonNull String prefix) {
        try {
            final var builderClass = Class.forName("java.lang.Thread$Builder");
            var builder = Thread.class.getMethod("ofVirtual").invoke(null);
            builder = builderClass.getMethod("name", String.class, long.class).invoke(builder, prefix + "-virtual-thread-",
                    1L);
            final var factory = (ThreadFactory) builderClass.getMethod("factory").invoke(builder);
            return Optional.of((ExecutorService) Executors.class.getMethod("newThreadPerTaskExecutor", ThreadFactory.class)
                    .invoke(null, factory));
        } catch (ReflectiveOperationException | RuntimeException e) {
            log.warnf("Virtual threads are not supported by Java %s, using a platform thread pool instead",
                    Runtime.version());
            log.debug("Failed to create virtual thread executor", e);
            return Optional.empty();
        }
    }

    private static @NonNull ThreadFactory threadFactory(@NonNull String prefix) {
        final var threadNumber = new AtomicInteger();
        return runnable -> {
            final var thread = new Thread(runnable, prefix + "-thread-" + threadNumber.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }
}
