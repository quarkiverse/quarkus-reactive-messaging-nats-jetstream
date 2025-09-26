package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher.PublishListener;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Health;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.providers.helpers.MultiUtils;
import lombok.extern.jbosslog.JBossLog;
import org.jspecify.annotations.NonNull;

@JBossLog
public class MessageSubscriberProcessor<T> implements MessageProcessor, PublishListener {
    private final String channel;
    private final String stream;
    private final String subject;
    private final Client client;

    private final AtomicReference<Health> health;

    public MessageSubscriberProcessor(final String channel,
            final String stream,
            final String subject,
            final Client client) {
        this.channel = channel;
        this.stream = stream;
        this.subject = subject;
        this.client = client;
        this.health = new AtomicReference<>(new Health(true, "Subscriber processor inactive"));
    }

    public Flow.Subscriber<Message<T>> subscriber() {
        return MultiUtils.via(this::subscribe);
    }

    private Multi<Message<T>> subscribe(Multi<Message<T>> subscription) {
        return subscription.onItem().transformToUniAndConcatenate(this::publish);
    }

    @Override
    public String channel() {
        return channel;
    }

    @Override
    public String stream() {
        return stream;
    }

    @Override
    public Health health() {
        return health.get();
    }



    private Uni<Message<T>> publish(final Message<T> message) {
        return getOrEstablishConnection()
                .onItem()
                .transformToUni(connection -> connection.publish(message, stream, subject))
                .onFailure()
                .invoke(failure -> log.errorf(failure, "Failed to publish with message: %s", failure.getMessage()))
                .onFailure().recoverWithUni(() -> recover(message));
    }

    private Uni<Message<T>> recover(final Message<T> message) {
        return Uni.createFrom().<Void> item(() -> {
            close();
            return null;
        })
                .onItem().transformToUni(v -> publish(message));
    }

    private Uni<? extends Client> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Client::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(() -> clientFactory.create(connectionConfiguration, this))
                .onItem().invoke(this.connection::set);
    }
}
