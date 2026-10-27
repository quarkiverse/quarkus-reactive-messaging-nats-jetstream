package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ThreadPoolConfiguration;

class ClientThreadPoolTest {

    @Test
    void startsThreadsUpToMaxSizeBeforeQueueing() throws Exception {
        try (final var threadPool = ClientThreadPool.of(configuration(1, 3, null))) {
            final Set<String> threadNames = ConcurrentHashMap.newKeySet();
            final var started = new CountDownLatch(3);
            final var release = new CountDownLatch(1);
            for (int i = 0; i < 3; i++) {
                threadPool.executorService().execute(() -> {
                    threadNames.add(Thread.currentThread().getName());
                    started.countDown();
                    await(release);
                });
            }

            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(threadNames).hasSize(3).allMatch(name -> name.matches("nats-client-\\d+-thread-\\d+"));
            release.countDown();
        }
    }

    @Test
    void rejectsTasksWhenQueueIsFull() {
        try (final var threadPool = ClientThreadPool.of(configuration(1, 1, 1))) {
            final var release = new CountDownLatch(1);
            threadPool.executorService().execute(() -> await(release));
            threadPool.executorService().execute(() -> {
            });

            assertThatThrownBy(() -> threadPool.executorService().execute(() -> {
            })).isInstanceOf(RejectedExecutionException.class);
            release.countDown();
        }
    }

    @Test
    void runsTasksOnVirtualThreads() throws Exception {
        try (final var threadPool = ClientThreadPool.of(configuration(true, 1, 1, null))) {
            final var thread = threadPool.executorService().submit(Thread::currentThread).get(5, TimeUnit.SECONDS);

            assertThat(thread.isVirtual()).isTrue();
            assertThat(thread.getName()).matches("nats-client-\\d+-virtual-thread-\\d+");
        }
    }

    @Test
    void closeShutsDownThePool() {
        final var threadPool = ClientThreadPool.of(configuration(1, 2, null));
        threadPool.close();

        assertThat(threadPool.executorService().isTerminated()).isTrue();
    }

    @Test
    void rejectsCoreSizeAboveMaxSize() {
        assertThatThrownBy(() -> ClientThreadPool.of(configuration(4, 2, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static ThreadPoolConfiguration configuration(int coreSize, int maxSize, Integer queueSize) {
        return configuration(false, coreSize, maxSize, queueSize);
    }

    private static ThreadPoolConfiguration configuration(boolean virtualThreads, int coreSize, int maxSize,
            Integer queueSize) {
        return new ThreadPoolConfiguration() {
            @Override
            public boolean virtualThreads() {
                return virtualThreads;
            }

            @Override
            public int coreSize() {
                return coreSize;
            }

            @Override
            public @NonNull Optional<Integer> maxSize() {
                return Optional.of(maxSize);
            }

            @Override
            public @NonNull Optional<Integer> queueSize() {
                return Optional.ofNullable(queueSize);
            }

            @Override
            public @NonNull Duration keepAlive() {
                return Duration.ofSeconds(30);
            }

            @Override
            public @NonNull Duration shutdownTimeout() {
                return Duration.ofSeconds(5);
            }
        };
    }
}
