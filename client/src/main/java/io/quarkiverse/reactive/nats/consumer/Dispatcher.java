package io.quarkiverse.reactive.nats.consumer;

import io.quarkiverse.reactive.nats.jetstream.Context;
import io.quarkiverse.reactive.nats.jetstream.message.MessageHandler;
import io.smallrye.mutiny.Uni;

import java.util.concurrent.ExecutorService;

/**
 * @see io.nats.client.Dispatcher
 */
public interface Dispatcher extends Consumer {

    static Dispatcher of(ImperativeDispatcher dispatcher, Context context, ExecutorService executor) {
        return new DispatcherDelegate(dispatcher, context, executor);
    }

    /**
     * @see io.nats.client.Dispatcher#start(String)
     */
    Uni<Void> start(String id);

    /**
     * @see io.nats.client.Dispatcher#subscribe(String)
     */
    Uni<Dispatcher> subscribe(String subject);

    /**
     * @see io.nats.client.Dispatcher#subscribe(String, String)
     */
    Uni<Dispatcher> subscribe(String subject, String queue);

    /**
     * @see io.nats.client.Dispatcher#subscribe(String, io.nats.client.MessageHandler)
     */
    Uni<Subscription> subscribe(String subject, MessageHandler handler);

    /**
     * @see io.nats.client.Dispatcher#subscribe(String, String, io.nats.client.MessageHandler)
     */
    Uni<Subscription> subscribe(String subject, String queue, MessageHandler handler);

    /**
     * @see io.nats.client.Dispatcher#unsubscribe(String)
     */
    Uni<Dispatcher> unsubscribe(String subject);

    /**
     * @see io.nats.client.Dispatcher#unsubscribe(io.nats.client.Subscription)
     */
    Uni<Dispatcher> unsubscribe(Subscription subscription);

    /**
     * @see io.nats.client.Dispatcher#unsubscribe(String, int)
     */
    Uni<Dispatcher> unsubscribe(String subject, int after);

    /**
     * @see io.nats.client.Dispatcher#unsubscribe(io.nats.client.Subscription, int)
     */
    Uni<Dispatcher> unsubscribe(Subscription subscription, int after);

}
