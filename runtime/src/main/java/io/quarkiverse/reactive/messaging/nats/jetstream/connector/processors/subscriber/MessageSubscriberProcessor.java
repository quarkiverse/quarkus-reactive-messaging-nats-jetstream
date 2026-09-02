package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber;

import java.time.Duration;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.Health;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageProcessor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.providers.helpers.MultiUtils;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class MessageSubscriberProcessor<T> implements MessageProcessor {
    private final String channel;
    private final String stream;
    private final String subject;
    private final Duration retryBackoff;

    private final AtomicReference<Health> health;
    private final Client client;

    private volatile boolean stopped;

    public MessageSubscriberProcessor(@NonNull final String channel,
            @NonNull final String stream,
            @NonNull final String subject,
            @NonNull Client client,
            @NonNull final Duration retryBackoff) {
        this.channel = channel;
        this.stream = stream;
        this.subject = subject;
        this.client = client;
        this.retryBackoff = retryBackoff;
        this.health = new AtomicReference<>(new Health(true, "Subscriber processor inactive"));
        this.stopped = false;
    }

    public Flow.Subscriber<Message<T>> subscriber() {
        return MultiUtils.via(this::subscribe);
    }

    private Multi<Message<T>> subscribe(Multi<Message<T>> subscription) {
        return subscription.onItem().transformToUniAndMerge(this::publish)
                .onItem().invoke(() -> health.set(new Health(true, "Subscriber processor active for channel: " + channel())))
                .onFailure().invoke(throwable -> health.set(new Health(false,
                        "Subscriber processor error for channel: " + channel() + " with message: " + throwable.getMessage())))
                .onFailure().retry().withBackOff(retryBackoff).until(failure -> stopped);
    }

    @Override
    public @NonNull String channel() {
        return channel;
    }

    @Override
    public @NonNull String stream() {
        return stream;
    }

    @Override
    public @NonNull Health health() {
        return health.get();
    }

    @Override
    public void stop() {
        this.stopped = true;
    }

    private Uni<Message<T>> publish(Message<T> message) {
        return client.publish(message, stream, subject);
    }
}
