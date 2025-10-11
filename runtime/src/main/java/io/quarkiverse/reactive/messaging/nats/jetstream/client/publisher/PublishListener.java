package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ErrorListener;
import org.eclipse.microprofile.reactive.messaging.Message;

public interface PublishListener<T> extends ErrorListener {

    void onPublished(Message<T> message);

}
