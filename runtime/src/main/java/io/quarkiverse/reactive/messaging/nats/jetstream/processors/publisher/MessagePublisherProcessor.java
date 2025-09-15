package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Health;
import io.smallrye.mutiny.Multi;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@JBossLog
public abstract class MessagePublisherProcessor<T> implements MessageProcessor {
    private final String channel;
    private final String stream;
    private final String consumer;
    private final AtomicReference<Health> readiness;
    private final AtomicReference<Health> liveness;
    private final Client client;
    private final Duration retryBackoff;

    public MessagePublisherProcessor(final String channel,
            final String stream,
            final String consumer,
            final Client client,
            final ConnectionConfiguration connectionConfiguration,
            final Duration retryBackoff) {
        this.channel = channel;
        this.stream = stream;
        this.consumer = consumer;
        this.readiness = new AtomicReference<>(
                Health.builder().event(ConnectionEvent.Closed).message("Publish processor inactive").healthy(false).build());
        this.liveness = new AtomicReference<>(
                Health.builder().event(ConnectionEvent.Closed).message("Publish processor inactive").healthy(false).build());
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
    public Health readiness() {
        return readiness.get();
    }

    @Override
    public Health liveness() {
        return liveness.get();
    }

    public Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> publisher() {
        return subscribe()
                .onFailure()
                .invoke(failure -> log.errorf(failure, "Failed to subscribe with message: %s", failure.getMessage()))
                .onFailure().retry().withBackOff(retryBackoff).indefinitely();
    }

    protected abstract Multi<Message<T>> subscription(Client client);

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe() {
        return getOrEstablishConnection()
                .onItem().transformToMulti(this::subscription)
                .onSubscription().invoke(() -> log.infof("Subscribed to channel %s", channel));
    }
}
