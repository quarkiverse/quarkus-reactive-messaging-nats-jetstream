package io.quarkiverse.reactive.nats.consumer;

import io.quarkiverse.reactive.nats.message.Message;
import io.smallrye.mutiny.Uni;

import java.time.Duration;

/**
 * @see io.nats.client.Subscription
 */
public interface Subscription extends Consumer {


    /**
     * @see io.nats.client.Subscription#getSubject()
     */
    Uni<String> getSubject();

    /**
     * @see io.nats.client.Subscription#getQueueName()
     */
    Uni<String> getQueueName();

    /**
     * @see io.nats.client.Subscription#getDispatcher()
     */
    Uni<Dispatcher> getDispatcher();

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

}
