package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.nats.client.Message;
import io.smallrye.mutiny.Uni;

interface PullMessage {

    Uni<Message> next();

    void close();
}
