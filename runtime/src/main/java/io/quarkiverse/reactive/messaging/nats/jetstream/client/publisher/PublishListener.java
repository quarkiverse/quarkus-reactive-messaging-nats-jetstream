package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ErrorListener;

public interface PublishListener extends ErrorListener {

    void onPublished(String messageId, Long sequence);

}
