package io.quarkiverse.reactive.nats.consumer;

import io.quarkiverse.reactive.nats.Context;
import io.quarkiverse.reactive.nats.consumer.imperative.ImperativeSubscription;
import io.quarkiverse.reactive.nats.message.Message;
import io.smallrye.mutiny.Uni;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

/**
 * @see io.nats.client.Subscription
 */
public interface Subscription extends Consumer {

    static Subscription of(ImperativeSubscription subscription, Context context, ExecutorService executor) {
        return new SubscriptionDelegate(subscription, context, executor);
    }

    /**
     * @see io.nats.client.Subscription#getSubject()
     */
    String getSubject();

    /**
     * @see io.nats.client.Subscription#getQueueName()
     */
    String getQueueName();

    /**
     * @see io.nats.client.Subscription#getDispatcher()
     */
    Dispatcher getDispatcher();

    /**
     * @see io.nats.client.Subscription#nextMessage(java.time.Duration)
     */
    Uni<Message> nextMessage(Duration timeout);

    /**
     * @see io.nats.client.Subscription#nextMessage(long)
     */
    Uni<Message> nextMessage(long timeoutMillis);

    /**
     * @see io.nats.client.Subscription#unsubscribe()
     */
    Uni<Void> unsubscribe();

    /**
     * @see io.nats.client.Subscription#unsubscribe(int)
     */
    Uni<Subscription> unsubscribe(int after);

    ImperativeSubscription delegate();
}
