package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.publisher;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.Health;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageProcessor;
import io.smallrye.mutiny.Multi;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class MessagePublisherProcessor<T> implements MessageProcessor {
    private final String channel;
    private final String stream;
    private final String consumer;
    private final Client client;
    private final Integer batchSize;
    private final Duration timeout;

    private final AtomicReference<Health> health;
    private final Duration retryBackoff;
    private final Class<T> payloadType;
    private volatile boolean stopped;

    public MessagePublisherProcessor(@NonNull final String channel,
            @NonNull final String stream,
            @NonNull final String consumer,
            @NonNull final Integer batchSize,
            @NonNull final Duration timeout,
            @NonNull final Client client,
            @NonNull final Duration retryBackoff,
            @Nullable final Class<T> payloadType) {
        this.channel = channel;
        this.stream = stream;
        this.consumer = consumer;
        this.batchSize = batchSize;
        this.timeout = timeout;
        this.client = client;
        this.retryBackoff = retryBackoff;
        this.payloadType = payloadType;

        this.health = new AtomicReference<>(Health.builder().message("Publish processor inactive").healthy(false).build());
        this.stopped = false;
    }

    @Override
    public @NonNull String channel() {
        return channel;
    }

    @Override
    public @NonNull String stream() {
        return stream;
    }

    public String consumer() {
        return consumer;
    }

    @Override
    public @NonNull Health health() {
        return health.get();
    }

    @Override
    public void stop() {
        this.stopped = true;
    }

    public Multi<Message<T>> publisher() {
        return subscribe()
                .onItem().invoke(() -> log.debugf("Received message from channel: %s", channel()))
                .onSubscription()
                .invoke(() -> health
                        .set(new Health(true, String.format("Publish processor healthy for channel: %s", channel()))))
                .onFailure().invoke(failure -> {
                    log.errorf(failure, "An error occurred with message: %s", failure.getMessage());
                    health.set(new Health(false, String.format("Publish processor unhealthy for channel: %s", channel())));
                })
                .onFailure().retry().withBackOff(retryBackoff).until(failure -> stopped);

    }

    private Multi<Message<T>> subscribe() {
        return payloadType == null ? client.subscribe(stream, consumer, timeout, batchSize)
                : client.subscribe(stream, consumer, timeout, batchSize, payloadType);
    }
}
