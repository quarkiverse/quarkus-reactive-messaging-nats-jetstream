package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
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
 * to the maximum size before queueing tasks, or by an executor starting a new virtual thread per task.
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
            return new ClientThreadPool(
                    Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name(prefix + "-virtual-thread-", 1).factory()),
                    configuration.shutdownTimeout());
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

    private static @NonNull ThreadFactory threadFactory(@NonNull String prefix) {
        final var threadNumber = new AtomicInteger();
        return runnable -> {
            final var thread = new Thread(runnable, prefix + "-thread-" + threadNumber.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }
}
