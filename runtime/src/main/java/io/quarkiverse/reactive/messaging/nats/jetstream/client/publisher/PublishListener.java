package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ErrorListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionListener;

public interface PublishListener extends ConnectionListener, ErrorListener {

    void onPublished(String stream, String subject, Long sequence);

}
