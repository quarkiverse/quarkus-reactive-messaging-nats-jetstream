package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ErrorListener;

public interface ConsumerListener<T> extends ErrorListener {

    /**
     * Called when a message is consumed
     */
    void onMessage(Message<T> message);

}
