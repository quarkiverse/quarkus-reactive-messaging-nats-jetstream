package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.smallrye.mutiny.Uni;

public interface Tracer<T> {

    Uni<SubscribeMessage<T>> withTrace(Message<T> message, PublishConfiguration configuration);

    Uni<Message<T>> withTrace(Message<T> message);

}
