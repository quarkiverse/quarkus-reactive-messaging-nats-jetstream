package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ErrorListener;
import org.eclipse.microprofile.reactive.messaging.Message;

public interface ConsumerListener<T> extends ErrorListener {

    /**
     * Called when a message is consumed
     */
    void onMessage(Message<T> message);

}
