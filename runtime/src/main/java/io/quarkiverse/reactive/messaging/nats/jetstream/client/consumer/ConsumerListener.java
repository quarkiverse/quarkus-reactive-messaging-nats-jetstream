package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ErrorListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionListener;
import org.eclipse.microprofile.reactive.messaging.Message;

public interface ConsumerListener<T> extends ConnectionListener, ErrorListener {

    void onMessage(Message<T> message);

}
