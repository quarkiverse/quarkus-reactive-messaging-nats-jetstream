package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Health;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.smallrye.mutiny.Multi;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public abstract class MessagePublisherProcessor<T> implements MessageProcessor, ConsumerListener<T> {
    private final String channel;
    private final String stream;
    private final String consumer;
    private final AtomicReference<Health> health;
    private final Duration retryBackoff;
    private final Class<T> payloadType;

    public MessagePublisherProcessor(final String channel,
            final String stream,
            final String consumer,
            final Duration retryBackoff,
            final Class<T> payloadType) {
        this.channel = channel;
        this.stream = stream;
        this.consumer = consumer;
        this.health = new AtomicReference<>(Health.builder().message("Publish processor inactive").healthy(false).build());
        this.retryBackoff = retryBackoff;
        this.payloadType = payloadType;
    }

    @Override
    public String channel() {
        return channel;
    }

    @Override
    public String stream() {
        return stream;
    }

    public String consumer() {
        return consumer;
    }

    @Override
    public Health health() {
        return health.get();
    }

    @Override
    public void onMessage(Message<T> message) {
        log.debugf("Publishing message to channel: %s", channel());
    }

    @Override
    public void onError(Throwable throwable) {
        log.errorf(throwable, "An error occurred with message: %s", throwable.getMessage());
        health.set(new Health(false, String.format("Publish processor unhealthy for channel: %s", channel())));
    }

    public Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> publisher() {
        return subscribe(payloadType)
                .onSubscription()
                .invoke(() -> health
                        .set(new Health(true, String.format("Publish processor healthy for channel: %s", channel()))))
                .onFailure()
                .invoke(failure -> log.errorf(failure, "Failed to subscribe with message: %s", failure.getMessage()))
                .onFailure().retry().withBackOff(retryBackoff).indefinitely();
    }

    protected abstract Multi<Message<T>> subscribe(Class<T> payloadType);
}
