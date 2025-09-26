package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Health;
import io.smallrye.mutiny.Multi;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@JBossLog
public abstract class MessagePublisherProcessor<T> implements MessageProcessor, ConsumerListener<T> {
    private final String channel;
    private final String stream;
    private final String consumer;
    private final AtomicReference<Health> health;
    private final Client client;
    private final Duration retryBackoff;

    public MessagePublisherProcessor(final String channel,
                                     final String stream,
                                     final String consumer,
                                     final Client client,
                                     final Duration retryBackoff) {
        this.channel = channel;
        this.stream = stream;
        this.consumer = consumer;
        this.health = new AtomicReference<>(Health.builder().message("Publish processor inactive").healthy(false).build());
        this.client = client;
        this.retryBackoff = retryBackoff;
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
    public void onConnected(Connection connection) {
        health.set(Health.builder().message(String.format("Connected to stream: %s and consumer: %s", stream(), consumer())).healthy(true).build());
    }

    @Override
    public void onError(Throwable throwable) {
        health.set(Health.builder().message(throwable.getMessage()).healthy(false).build());
    }

    public Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> publisher() {
        return subscribe(() -> client)
                .onSubscription().invoke(() -> log.infof("Subscribed to channel %s", channel))
                .onFailure()
                .invoke(failure -> log.errorf(failure, "Failed to subscribe with message: %s", failure.getMessage()))
                .onFailure().retry().withBackOff(retryBackoff).indefinitely();
    }

    protected abstract Multi<Message<T>> subscribe(Supplier<Client> client);
}
