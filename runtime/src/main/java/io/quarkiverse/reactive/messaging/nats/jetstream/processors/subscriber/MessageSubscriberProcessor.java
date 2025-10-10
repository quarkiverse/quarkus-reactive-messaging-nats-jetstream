package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import java.time.Duration;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher.PublishListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Health;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.providers.helpers.MultiUtils;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class MessageSubscriberProcessor<T> implements MessageProcessor, PublishListener {
    private final String channel;
    private final String stream;
    private final String subject;
    private final Client client;
    private final Duration retryBackoff;

    private final AtomicReference<Health> health;

    public MessageSubscriberProcessor(final String channel,
            final String stream,
            final String subject,
            final Client client,
            final Duration retryBackoff) {
        this.channel = channel;
        this.stream = stream;
        this.subject = subject;
        this.client = client;
        this.retryBackoff = retryBackoff;
        this.health = new AtomicReference<>(new Health(true, "Subscriber processor inactive"));
    }

    public Flow.Subscriber<Message<T>> subscriber() {
        return MultiUtils.via(this::subscribe);
    }

    private Multi<Message<T>> subscribe(Multi<Message<T>> subscription) {
        return client.publish(subscription, stream, subject, this)
                .onFailure().retry().withBackOff(retryBackoff).indefinitely();
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

    @Override
    public void onPublished(String messageId, Long sequence) {
        log.debugf("Published message with id: %s and sequence: %s to stream: %s and subject: %s", messageId, sequence, stream,
                subject);
        health.set(new Health(true, "Subscriber processor active for channel: " + channel()));
    }

    @Override
    public void onError(Throwable throwable) {
        health.set(new Health(false,
                "Subscriber processor error for channel: " + channel() + " with message: " + throwable.getMessage()));
    }
}
