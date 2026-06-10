package io.quarkiverse.reactive.messaging.nats.client.publisher;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.client.ErrorListener;

public interface PublishListener<T> extends ErrorListener {

    void onPublished(Message<T> message);

}
